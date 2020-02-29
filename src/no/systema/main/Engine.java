package no.systema.main;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author oscardelatorre 
 * @date March, 2020
 * 
 * 
 * This class is entry point and driving force of the application
 * It calls a service (rest-template) in order to start with the uploading of one or several kurermanifest file payloads.
 * 
 * Note that the called service does deliver a content of a file (payload) and NOT the file or a file-path itself.
 * All payloads are delivered by means of a rest-service (through the Spring RestTemplate)
 */
public class Engine {
	
	/**
	 * Call from prompt: 
	 * TEST: java -jar upload-engine-kurermanif-client.jar http://localhost:8080/syjservicestn-expft/testUpload
	 * PRODUCTION: java -jar upload-engine-kurermanif-client.jar http://localhost:8080/syjservicestn-expft/prodUpload?user=A53HUR
	 * 
	 * The parameter [0] must be the url to the init rest_service on tomcat.
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		 
		Engine engine = new Engine();
		engine.execute(args);
		
	}
	
	public void execute(String[] args) throws Throwable {
		long sleep = 300000;
		long numberOfTimes = 288;
		
		//TEST
		if(args!=null && args.length>1){
			if(args[1].equals("1")){
				sleep = 5000; 
				/*every 5 minutes = 12 times per hour. 24hrs * 12 times = 288 times */
				numberOfTimes = 3; 
			}
		}
		 
		
		String urlParam = args[0];
		
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		ScheduledFuture future = executor.scheduleWithFixedDelay(new PollingService(urlParam), 0, sleep,TimeUnit.MILLISECONDS);
		Thread.sleep(numberOfTimes * sleep);
		future.cancel(false);
		executor.shutdown();
	 }
	 
	class PollingService implements Runnable {
		private Logger logger = Logger.getLogger(PollingService.class);
		private URI uri = null;
		private int count = 0;
		
		PollingService(String url) throws Throwable{ uri = new URI(url); }
		 
		
		public void run() {
			try {
				
				logger.info("iteration :" + (count++));
				//call rest controller to start polling of directories for file upload
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> response = new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
				
				response = restTemplate.getForEntity(uri, String.class);
				logger.info("response="+response);
			} catch (Exception e) {
				logger.info("There is space for improvements on indata..." + e.toString());
				e.printStackTrace();
			}
	 	}
	}
}
