package dev.luin.file.client.core.server.servlet;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class Health extends GenericServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
	{
		((HttpServletResponse)res).setStatus(HttpServletResponse.SC_OK);
	}
}
