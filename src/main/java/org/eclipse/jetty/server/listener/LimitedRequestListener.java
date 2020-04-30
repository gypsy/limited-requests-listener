package org.eclipse.jetty.server.listener;

import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class LimitedRequestListener implements HttpChannel.Listener {

	private static final Logger LOG = Log.getLogger(LimitedRequestListener.class);

	private static final String CONN_CLOSE_HEADER_NAME = "Connection";
	private static final String CONN_CLOSE_HEADER_VAL = "Close";
	private static final String JETTY_REQ_COUNT_ATTRIB = "org.eclipse.jetty.LimitedRequestListener.RequestCount";

	private int maxRequests = 10;

	public int getMaxRequests() {
		return maxRequests;
	}

	public void setMaxRequests(int maxRequests) {
		this.maxRequests = maxRequests;
	}

	@Override
	public void onRequestBegin(Request request) {
		// set the number of requests as an attribute to help correlate exceptions to closed connections
		request.setAttribute(JETTY_REQ_COUNT_ATTRIB, getRequestCount(request));
	}

	@Override
	public void onResponseBegin(Request request) {
		long requests = getRequestCount(request);
		// After X responses, forcibly set connection close on response
		if (requests >= maxRequests) {
			request.getResponse().setHeader(CONN_CLOSE_HEADER_NAME, CONN_CLOSE_HEADER_VAL);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Setting [Connection: Close] on Request #{} for {}", requests, request.getHttpChannel().getEndPoint().getTransport());
			}
		}
	}

	@Override
	public void onComplete(Request request) {
		long requests = getRequestCount(request);
		// forcibly close connection after X request and responses are complete
		if (requests > maxRequests) {
			request.getHttpChannel().getEndPoint().close();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Forcing Close on Request #{} for {}", requests, request.getHttpChannel().getEndPoint().getTransport());
			}
		}
	}

	private static long getRequestCount(Request request) {
		return request.getHttpChannel().getRequests();
	}
}