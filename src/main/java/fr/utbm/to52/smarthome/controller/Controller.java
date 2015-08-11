package fr.utbm.to52.smarthome.controller;

import java.io.IOException;
import java.util.Calendar;

import fr.utbm.to52.smarthome.events.AddRingEvent;
import fr.utbm.to52.smarthome.events.RingEvent;
import fr.utbm.to52.smarthome.model.cron.Cron;
import fr.utbm.to52.smarthome.network.MQTT;
import fr.utbm.to52.smarthome.network.SocketInput;
import it.sauronsoftware.cron4j.ProcessTask;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulingPattern;

/**
 * This controller is a singleton. For getting an instance from it 
 * <code>Controller.getInstance()</code> This initialize it with 
 * the default configuration file if it was not created before.
 * 
 * @author Alexandre Guyon
 *
 */
public class Controller {

	/**
	 * Singleton instance for this controller 
	 */
	private static Controller singleton = null;

	/**
	 * Get an instance from the controller. The first call initialize it with the default configuration file.
	 * @return Singleton instance of this controller
	 */
	public static Controller getInstance(){
		if(singleton == null)
			singleton = new Controller();
		return singleton;
	}
	
	/**
	 * Defining the source of the command is from network. tag configuration
	 */
	public static final int SOURCE_NET = 0;
	
	/**
	 * Defining the source of the command is from ICAL. tagICal configuration
	 */
	public static final int SOURCE_ICAL = 1;

	private SocketInput server;
	
	private Thread mainThread;

	private MQTT mqtt;

	private Conf config;

	private Cron cron;

	private Scheduler jcron;

	private CommandHandlerImpl cmdHandler;

	private Controller(){
		this.config = new Conf();
		this.config.importConf();
		this.cmdHandler = new CommandHandlerImpl();
		this.jcron = new Scheduler();
		this.mqtt = new MQTT(this.config.getMQTTID(), this.config.getMQTTServer(), this.config.getMQTTQOS(), this.cmdHandler);
	}

	/**
	 * Start the server
	 */
	public void start(){		
		this.confCron();

		this.mqtt.connect();

		this.cmdHandler.setRingEventController(new RingEvent(this.mqtt));
		this.cmdHandler.setAddRingEventController(new AddRingEvent());

		try {
			this.server = new SocketInput(this.getConfig().getServerPort());
			this.server.setCmdHandler(this.cmdHandler);

			this.mainThread = new Thread(this.server);
			this.mainThread.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.jcron.start();
	}

	private void confCron() {
		this.cron = new Cron();
		
//		if(this.getConfig().getCronSource() == Conf.CRON_JAVA){ // Set old crontab to java
//			for(int i = 0 ; i < this.cron.getCrontab().size() ; ++i)
//				this.jcron.schedule(this.cron.getSchedulingPattern(i), this.cron.getTask(i));
//		}
		
		for(int i = 0 ; i < this.getConfig().getAlarmURL().size() ; ++i)
			this.jcron.schedule("* * * * *", new DateController(i));
		
		this.jcron.schedule("0 * * * *", new CronCleaner(this.getCron()));
	}

	/**
	 * Stop the server
	 */
	public void stop(){
		this.server.setRunning(false);
		
		try {
			this.mainThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		this.jcron.stop();
		this.mqtt.disconnect();
	}

	/**
	 * Add ring in the crontab with the corresponding date
	 * @param c The date to ring
	 * @param INPUT Controller source
	 */
	public void addRing(Calendar c, int INPUT){
		ProcessTask p;
		
		String host = "localhost";
		String port = Controller.getInstance().getConfig().getServerPort() + "";
		String CMD = Controller.getInstance().getConfig().getCommandRing();
		
		if(INPUT == Controller.SOURCE_ICAL)
			p = new ProcessTask("echo " + CMD + " | nc " + host + " " + port
					+ " " + Controller.getInstance().getConfig().getCronICalTag());
		else
			p = new ProcessTask("echo " + CMD + " | nc " + host + " " + port
					+ " " + Controller.getInstance().getConfig().getCronTag());

		int min = c.get(Calendar.MINUTE);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int d = c.get(Calendar.DAY_OF_MONTH);
		int m = c.get(Calendar.MONTH) + 1;

		this.cron.add(new SchedulingPattern(min +" " + hour + " " + d + " " + m + " *" ), p);

		//this.cron.apply();
	}

	/**
	 * Get the configuration of the application loaded
	 * @return The configuration loaded.
	 */
	public Conf getConfig() {
		return this.config;
	}

	/**
	 * Set to the controller a special configuration
	 * @param config The configuration to set to the controller.
	 */
	public void setConfig(Conf config) {
		this.config = config;
	}
	
	/**
	 * @return the cron
	 */
	public Cron getCron() {
		return this.cron;
	}

	/**
	 * @param cron the cron to set
	 */
	public void setCron(Cron cron) {
		this.cron = cron;
	}

	/**
	 * @return the jcron
	 */
	public Scheduler getJcron() {
		return this.jcron;
	}

	/**
	 * @param jcron the jcron to set
	 */
	public void setJcron(Scheduler jcron) {
		this.jcron = jcron;
	}
}
