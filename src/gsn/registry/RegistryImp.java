package gsn.registry;

import gsn.pid.PIDUtils;
import gsn.shared.Registry;
import gsn.shared.VirtualSensorIdentityBean;
import gsn.utils.KeyValueImp;
import gsn.utils.ValidityTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.KeyValue;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * @author Ali Salehi (AliS, ali.salehi-at-epfl.ch)<br>
 */
public class RegistryImp extends HttpServlet implements Registry {
    /**
         * FIXME : Possible BUG, Since two thread are accessing the registery,
         * one is the directory service garbage collector and the others are the
         * remote clients, there existsthe possiblity of the concurrent
         * modification exception on the common shared object
         * <code>registery</code>
         */
    private static ArrayList<VirtualSensorIdentityBean> registry = new ArrayList<VirtualSensorIdentityBean>();

    public static final String DEFAULT_DIR_LOG4J_PROPERTIES = "conf/log4j.directory.properties";

    public static final String DEFAULT_DIRECTORY_SERVER_WEB_APP = "dswebapp";

    private static transient Logger logger;

    public static ArrayList<VirtualSensorIdentityBean> getRegistry() {
	return registry;
    }

    public void addVirtualSensor(
	    VirtualSensorIdentityBean newVirtualSensorIdentity) {
	registry.remove(newVirtualSensorIdentity);
	registry.add(newVirtualSensorIdentity);
    }

    public void removeVirtualSensor(VirtualSensorIdentityBean vsensor) {
	registry.remove(vsensor);
    }

    /**
         * @param registry
         *                The registry to set.
         */
    static void setRegistry(ArrayList<VirtualSensorIdentityBean> registry) {
	RegistryImp.registry = registry;
    }

    public ArrayList<VirtualSensorIdentityBean> findVSensor(
	    ArrayList<KeyValue> predicates) {
	ArrayList<VirtualSensorIdentityBean> result = new ArrayList<VirtualSensorIdentityBean>();
	for (VirtualSensorIdentityBean vsensor : registry)
	    if (vsensor.matches(predicates))
		result.add(vsensor);
	return result;
    }

    private static File pidFile;

    public static void main(String[] args) throws Exception {
	if (args.length < 2) {
	    System.out
		    .println("You must specify the port on which the directory service will listen (default 1882)");
	    System.out
		    .println("You must specify the interface IP on which the directory service will listen (default localhost)");
	    System.exit(1);
	}
	System.out.println("Loading logging details from : "
		+ DEFAULT_DIR_LOG4J_PROPERTIES);
	PropertyConfigurator.configure(DEFAULT_DIR_LOG4J_PROPERTIES);
	logger = Logger.getLogger(RegistryImp.class);
	if (PIDUtils.isPIDExist(PIDUtils.DIRECTORY_SERVICE_PID)) {
	    System.out
		    .println("Error : Another GSN Directory Service is running.");
	    System.exit(1);
	} else
	    pidFile = PIDUtils.createPID(PIDUtils.DIRECTORY_SERVICE_PID);
	int port = -1;
	try {
	    port = Integer.parseInt(args[0]);
	} catch (Exception e) {
	    logger.error("Can't parse the port no. from the input (" + args[0]
		    + ")", e);
	    return;
	}

	if (logger.isInfoEnabled())
	    logger.info("GSN-Registry-Server startup ");
	System.getProperties().put("org.mortbay.level", "error");
	String computerIP = args[1];
	if (!ValidityTools.isLocalhost(computerIP)) {
	    logger.fatal("The specified IP address (" + args[1]
		    + ") is not pointing to the local machine.");
	    return;
	}
	final Server server = new Server();
	Connector connector = new SelectChannelConnector();
	connector.setPort(port);
	server.setConnectors(new Connector[] { connector });

	WebAppContext wac = new WebAppContext();
	wac.setContextPath("/");
	wac.setResourceBase(DEFAULT_DIRECTORY_SERVER_WEB_APP);
	
	ServletHandler servletHandler = new ServletHandler();
	servletHandler.addServletWithMapping("gsn.registry.RegistryImp",
		"/registry");
	wac.setServletHandler(servletHandler);

	server.setHandler(wac);
	server.setStopAtShutdown(true);
	server.setSendServerVersion(false);
	server.start();

	final TheGarbageCollector garbageCollector = new TheGarbageCollector(
		new RegistryImp());
	garbageCollector.start();
	if (logger.isInfoEnabled())
	    logger.info("[ok]");
	Thread shutdown = new Thread(new Runnable() {
	    public void run() {
		try {
		    while (true) {
			int value = PIDUtils.getFirstIntFrom(pidFile);
			if (value != '0')
			    Thread.sleep(2500);
			else
			    break;
		    }
		    server.stop();
		    garbageCollector.stopPlease();
		} catch (Exception e) {
		    logger.warn("Shutdowning the webserver failed.", e);
		    System.exit(1);
		}
		logger.warn("GSN Directory server is stopped.");
		System.exit(0);
	    }
	});
	shutdown.start();

    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
	    throws ServletException, IOException {
	int requestType = Integer.parseInt(req.getHeader(Registry.REQUEST));
	VirtualSensorIdentityBean sensorIdentityBean;
	switch (requestType) {
	case Registry.REGISTER:
	    sensorIdentityBean = new VirtualSensorIdentityBean(req);
	    if (logger.isDebugEnabled())
		logger.debug(new StringBuilder().append(
			"Register request received for VSName : ").append(
			sensorIdentityBean.getVSName()).toString());
	    addVirtualSensor(sensorIdentityBean);
	    break;
	case Registry.DEREGISTER:
	    sensorIdentityBean = new VirtualSensorIdentityBean(req);
	    if (logger.isDebugEnabled())
		logger.debug(new StringBuilder().append(
			"Deregister request received for VSName : ").append(
			sensorIdentityBean.getVSName()).toString());
	    removeVirtualSensor(sensorIdentityBean);
	    break;
	case Registry.QUERY:
	    if (logger.isDebugEnabled())
		logger
			.debug("Query request received containg the following predicates : ");
	    Enumeration keys = req.getHeaders(Registry.VS_PREDICATES_KEYS);
	    Enumeration values = req.getHeaders(Registry.VS_PREDICATES_VALUES);
	    ArrayList<KeyValue> predicates = new ArrayList<KeyValue>();
	    while (keys.hasMoreElements()) {
		String key = (String) keys.nextElement();
		String value = (String) values.nextElement();
		if (logger.isDebugEnabled())
		    logger.debug(new StringBuilder().append("[key=")
			    .append(key).append(",value=").append(value)
			    .append("]").toString());
		predicates.add(new KeyValueImp(key, value));
	    }
	    ArrayList<VirtualSensorIdentityBean> vsQueryResult = findVSensor(predicates);
	    if (logger.isDebugEnabled())
		logger.debug(new StringBuilder().append(
			"The query resulted in ").append(vsQueryResult.size())
			.append(" results.").toString());
	    fillQueryRespond(res, vsQueryResult);
	    break;
	default:
	    if (logger.isInfoEnabled())
		logger
			.info("Request received at the register with unknow request type !!!");
	}
    }

    private void fillQueryRespond(HttpServletResponse res,
	    ArrayList<VirtualSensorIdentityBean> vsensors) {
	for (VirtualSensorIdentityBean vsensor : vsensors) {
	    res.addHeader(Registry.VS_NAME, vsensor.getVSName());
	    res.addHeader(Registry.VS_PORT, Integer.toString(vsensor
		    .getRemotePort()));
	    res.addHeader(Registry.VS_HOST, vsensor.getRemoteAddress());
	}
    }
}

/**
 * @author Ali Salehi (AliS, ali.salehi-at-epfl.ch)<br>
 */
class TheGarbageCollector extends Thread {

    private final transient Logger logger = Logger
	    .getLogger(TheGarbageCollector.class);

    private final int INTERVAL = 5 * 60 * 1000; // each 5 mins

    private RegistryImp registerImplemation;

    private boolean toStop;

    public TheGarbageCollector(RegistryImp impl) {
	this.registerImplemation = impl;
	this.toStop = false;
    }

    public void run() {
	while (!toStop) {
	    ArrayList<VirtualSensorIdentityBean> registery = (ArrayList<VirtualSensorIdentityBean>) registerImplemation
		    .getRegistry().clone();
	    long current = System.currentTimeMillis();
	    Iterator<VirtualSensorIdentityBean> it = registery.iterator();
	    while (it.hasNext()) {
		VirtualSensorIdentityBean virtualSensorIdentityBean = it.next();
		if ((current - virtualSensorIdentityBean.getLatestVisit()) > INTERVAL)
		    it.remove();
		else
		    virtualSensorIdentityBean.setLatestVisit(current);
	    }
	    registerImplemation.setRegistry(registery);
	    try {
		sleep(INTERVAL);
	    } catch (InterruptedException e) {
		if (toStop)
		    return;
		logger.error(e.getMessage(), e);
	    }
	}
    }

    public void stopPlease() {
	toStop = true;
	interrupt();
    }
}
