package org.eclipse.jetty.server.listener;

import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;

public class LimitedRequestListener implements HttpChannel.Listener {

	private static final String CONN_CLOSE_HEADER_NAME = "Connection";
	private static final String CONN_CLOSE_HEADER_VAL = "Close";

	private int maxRequests = 10;

	public int getMaxRequests() {
		return maxRequests;
	}

	public void setMaxRequests(int maxRequests) {
		this.maxRequests = maxRequests;
	}

	@Override
	public void onResponseBegin(Request request) {
		long requests = request.getHttpChannel().getRequests();
		// After X responses, forcibly set connection close on response
		if (requests >= maxRequests) {
			request.getResponse().setHeader(CONN_CLOSE_HEADER_NAME, CONN_CLOSE_HEADER_VAL);
		}
	}

	@Override
	public void onComplete(Request request) {
		long requests = request.getHttpChannel().getRequests();
		// forcibly close connection after X request and responses are complete
		if (requests > maxRequests) {
			request.getHttpChannel().getEndPoint().close();
		}
	}
}