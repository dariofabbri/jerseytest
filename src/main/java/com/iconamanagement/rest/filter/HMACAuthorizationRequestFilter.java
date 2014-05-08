package com.iconamanagement.rest.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iconamanagement.service.ServiceFactory;
import com.iconamanagement.service.crypto.UserKeyService;
import com.iconamanagement.service.sqlsessionfactory.SqlSessionFactoryService;

@Provider
public class HMACAuthorizationRequestFilter implements ContainerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(HMACAuthorizationRequestFilter.class);
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		SqlSessionFactoryService service = ServiceFactory.getService(SqlSessionFactoryService.class);
		service.getSqlSession();
		
		// Check if Date header is present.
		//
		String dateHeader = requestContext.getHeaderString("Date");
		if(dateHeader == null) {
			dateHeader = requestContext.getHeaderString("X-Date");
		}
		if (dateHeader == null) {
			requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Missing Date header.")
                    .build());
			return;
		}
		
		// Parse Date header into a java.util.Date object.
		//
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = null;
		try {
			date = dateFormat.parse(dateHeader);
		} catch (ParseException e) {
			requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid format detected in Date header.")
                    .build());
			return;
		}
		
		// Check if request date is too skewed compared with server's time.
		//
		Date now = new Date();
		if(date.after(now) || date.before(DateUtils.addMinutes(now, -15))) {
			requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Request time too skewed.")
                    .build());
			return;
		}

		// Check if the Authorization header is present.
		//
		String authorizationHeader = requestContext.getHeaderString("Authorization");
		if (authorizationHeader == null) {
			requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Missing Authorization header.")
                    .build());
			return;
		}

		// The accepted structure for Authorization header is:
		//
		//   ICONA-HMAC-SHA256 username-base64:signature-base64
		//
		Pattern pattern = Pattern.compile("^ICONA-HMAC-SHA256 ([A-Za-z0-9+/=]+):([A-Za-z0-9+/=]+)$");
		Matcher matcher = pattern.matcher(authorizationHeader);
		if(!matcher.matches()) {
			requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid Authorization header structure.")
                    .build());
			return;			
		}
		
		// Extract the username and the signature fields from the header.
		//
		String username = null;
		byte[] signature = null;
		try {
			username = new String(Base64.getDecoder().decode(matcher.group(1)), "UTF8");
			signature = Base64.getDecoder().decode(matcher.group(2));
		} catch(Exception e) {
			logger.error("Exception caught while decoding Base64 Authorization header.", e);
			requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid Base64 encoding detected in Authorization header.")
                    .build());
			return;			
		}
		
		// Retrieve the key of the specified user.
		//
		UserKeyService userKeyService = ServiceFactory.getService(UserKeyService.class);
		byte[] key = userKeyService.getUserKey(username);
		System.out.println(new String(key, "UTF-8"));

		// Extract the body of the request, it will be used in the signature calculation.
		//
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = requestContext.getEntityStream();
		IOUtils.copy(is, baos);

		// Calculate the signature of the message. The content to be signed is the following:
		// 
		// HMAC(HMAC(request-uri) + HMAC(http-method) + HMAC(date-header) + HMAC(body))
		//
		byte[] calculatedSignature = null;
		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
			sha256_HMAC.init(secret_key);
			
			byte[] hmac1 = sha256_HMAC.doFinal(requestContext.getUriInfo().getRequestUri().toString().getBytes("UTF-8"));
			byte[] hmac2 = sha256_HMAC.doFinal(requestContext.getMethod().getBytes("UTF-8"));
			byte[] hmac3 = sha256_HMAC.doFinal(dateHeader.getBytes("UTF-8"));
			byte[] hmac4 = sha256_HMAC.doFinal(baos.toByteArray());
			
			ByteArrayOutputStream concat = new ByteArrayOutputStream();
			concat.write(hmac1);
			concat.write(hmac2);
			concat.write(hmac3);
			concat.write(hmac4);
			
			calculatedSignature = sha256_HMAC.doFinal(concat.toByteArray());
			concat.close();
			
		} catch(InvalidKeyException | NoSuchAlgorithmException e) {
			logger.error("Exception caught while calculating message signature for comparison.", e);
			requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Error detected while calculating message signature.")
                    .build());
			return;						
		}

		if(!Arrays.equals(signature, calculatedSignature)) {
			requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Authorization signature mismatch.")
                    .build());
			return;			
		}
		

		// Restore message body.
		//
		requestContext.setEntityStream(new ByteArrayInputStream(baos.toByteArray()));
	}

	
}
