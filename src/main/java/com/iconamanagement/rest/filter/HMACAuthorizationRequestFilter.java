package com.iconamanagement.rest.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.time.DateUtils;

import com.iconamanagement.service.ServiceFactory;
import com.iconamanagement.service.crypto.UserKeyService;
import com.iconamanagement.service.sqlsessionfactory.SqlSessionFactoryService;

@Provider
public class HMACAuthorizationRequestFilter implements ContainerRequestFilter {

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
		
		// Sign the message.
		//
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = requestContext.getEntityStream();
		IOUtils.copy(is, baos);

		requestContext.setEntityStream(new ByteArrayInputStream(baos.toByteArray()));
	}

	
}
