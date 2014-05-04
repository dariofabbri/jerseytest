package com.iconamanagement.rest.resource;

import java.util.ArrayList;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.iconamanagement.rest.dto.TestDto;

@Path("/helloworld")
public class HelloWorld {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TestDto getHello() {
		
		TestDto dto = new TestDto();
		dto.setMessage("Hello, world!");
		dto.setResult(1);
		
		dto.setNames(new ArrayList<String>());
		dto.getNames().add("Pippo");
		dto.getNames().add("Pluto");
		dto.getNames().add("Paperino");
		dto.setDate(new Date());
		
		return dto;
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createHello(TestDto hello) {
		
		return Response.ok("Created!").build();
	}
}
