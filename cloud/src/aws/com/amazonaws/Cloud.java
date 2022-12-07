package aws.com.amazonaws;

/*
* Cloud Computing
* 
* Dynamic Resource Management Tool
* using AWS Java SDK Library
* 
*/
import java.util.Iterator;
import java.util.Scanner;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Filter;



public class Cloud {

	static AmazonEC2      aws_ec2;

	private static void init() throws Exception {

		ProfileCredentialsProvider credential = new ProfileCredentialsProvider();
		try {
			credential.getCredentials();
		} catch (Exception exp) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
					"Please make sure that your credentials file is at the correct " +
					"location (~/.aws/credentials), and is in valid format.",
					exp);
		}
		aws_ec2 = AmazonEC2ClientBuilder.standard()
			.withCredentials(credential)
			.withRegion("us-east-1")	/* check the region at AWS console */
			.build();
	}
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {

		init();

		Scanner menu_int = new Scanner(System.in);
		Scanner ec2_id_str = new Scanner(System.in);
		int num = 0;
		
		while(true)
		{
			System.out.println("                                                            ");
			System.out.println("                                                            ");
			System.out.println("------------------------------------------------------------");
			System.out.println("           Amazon AWS Control Panel using SDK               ");
			System.out.println("------------------------------------------------------------");
			System.out.println("  1. list instance                2. available zones        ");
			System.out.println("  3. start instance               4. available regions      ");
			System.out.println("  5. stop instance                6. create instance        ");
			System.out.println("  7. reboot instance              8. list images            ");
			System.out.println("                                 99. quit                   ");
			System.out.println("------------------------------------------------------------");
			
			System.out.print("Enter an integer: ");
			
			if(menu_int.hasNextInt()){
				num = menu_int.nextInt();
				}else {
					System.out.println("concentration!");
					break;
				}
			

			String instance_id = "";

			switch(num) {
			case 1: 
				listInstances();
				break;
				
			case 2: 
				availableZones();
				break;
				
			case 3: 
				System.out.print("Enter instance id: ");
				if(ec2_id_str.hasNext())
					instance_id = ec2_id_str.nextLine();
				
				if(!instance_id.isEmpty()) 
					startInstance(instance_id);
				break;

			case 4: 
				availableRegions();
				break;

			case 5: 
				System.out.print("Enter instance id: ");
				if(ec2_id_str.hasNext())
					instance_id = ec2_id_str.nextLine();
				
				if(!instance_id.isEmpty()) 
					stopInstance(instance_id);
				break;

			case 6: 
				System.out.print("Enter ami id: ");
				String ami_id = "";
				if(ec2_id_str.hasNext())
					ami_id = ec2_id_str.nextLine();
				
				if(!ami_id.isEmpty()) 
					createInstance(ami_id);
				break;

			case 7: 
				System.out.print("Enter instance id: ");
				if(ec2_id_str.hasNext())
					instance_id = ec2_id_str.nextLine();
				
				if(!instance_id.isEmpty()) 
					rebootInstance(instance_id);
				break;

			case 8: 
				listImages();
				break;

			

			case 99: 
				System.out.println("bye!");
				menu_int.close();
				ec2_id_str.close();
				return;
			default: System.out.println("concentration!");
			}

		}
		
	}

	public static void listInstances() {
		
		System.out.println("Listing instances....");
		boolean done = false;
		
		DescribeInstancesRequest instance_request = new DescribeInstancesRequest();
		
		while(!done) {
			DescribeInstancesResult instance_response = aws_ec2.describeInstances(instance_request);

			for(Reservation reservation : instance_response.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
					System.out.printf(
						"[id] %s, " +
						"[AMI] %s, " +
						"[type] %s, " +
						"[state] %10s, " +
						"[monitoring state] %s",
						instance.getInstanceId(),
						instance.getImageId(),
						instance.getInstanceType(),
						instance.getState().getName(),
						instance.getMonitoring().getState());
				}
				System.out.println();
			}

			instance_request.setNextToken(instance_response.getNextToken());

			if(instance_response.getNextToken() == null) {
				done = true;
			}
		}
	}
	
	public static void availableZones()	{

		System.out.println("Available zones....");
		try {
			DescribeAvailabilityZonesResult availabilityZonesResult = aws_ec2.describeAvailabilityZones();
			Iterator <AvailabilityZone> iterator = availabilityZonesResult.getAvailabilityZones().iterator();
			
			AvailabilityZone zone;
			while(iterator.hasNext()) {
				zone = iterator.next();
				System.out.printf("[id] %s,  [region] %15s, [zone] %15s\n", zone.getZoneId(), zone.getRegionName(), zone.getZoneName());
			}
			System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
					" Availability Zones.");

		} catch (AmazonServiceException ser_exc) {
				System.out.println("Caught Exception: " + ser_exc.getMessage());
				System.out.println("Reponse Status Code: " + ser_exc.getStatusCode());
				System.out.println("Error Code: " + ser_exc.getErrorCode());
				System.out.println("Request ID: " + ser_exc.getRequestId());
		}
	
	}

	public static void startInstance(String instance_id)
	{
		
		System.out.printf("Starting .... %s\n", instance_id);
		final AmazonEC2 aws_ec2 = AmazonEC2ClientBuilder.defaultClient();

		DryRunSupportedRequest<StartInstancesRequest> dry_request =
		() -> {
			StartInstancesRequest start_request = new StartInstancesRequest()
				.withInstanceIds(instance_id);

			return start_request.getDryRunRequest();
		};

		StartInstancesRequest start_request = new StartInstancesRequest()
			.withInstanceIds(instance_id);

		aws_ec2.startInstances(start_request);

		System.out.printf("Successfully started instance %s", instance_id);
	}
	
	
	public static void availableRegions() {
		
		System.out.println("Available regions ....");
		
		final AmazonEC2 aws_ec2 = AmazonEC2ClientBuilder.defaultClient();

		DescribeRegionsResult regions_response = aws_ec2.describeRegions();

		for(Region region : regions_response.getRegions()) {
			System.out.printf(
				"[region] %15s, " +
				"[endpoint] %s\n",
				region.getRegionName(),
				region.getEndpoint());
		}
	}
	
	public static void stopInstance(String instance_id) {
		final AmazonEC2 aws_ec2 = AmazonEC2ClientBuilder.defaultClient();

		DryRunSupportedRequest<StopInstancesRequest> dry_request =
			() -> {
			StopInstancesRequest stop_request = new StopInstancesRequest()
				.withInstanceIds(instance_id);

			return stop_request.getDryRunRequest();
		};

		try {
			StopInstancesRequest stop_request = new StopInstancesRequest()
				.withInstanceIds(instance_id);
	
			aws_ec2.stopInstances(stop_request);
			System.out.printf("Successfully stop instance %s\n", instance_id);

		} catch(Exception exc)
		{
			System.out.println("Exception: "+exc.toString());
		}

	}
	
	public static void createInstance(String ami_id) {
		final AmazonEC2 aws_ec2 = AmazonEC2ClientBuilder.defaultClient();
		
		RunInstancesRequest run_request = new RunInstancesRequest()
			.withImageId(ami_id)
			.withInstanceType(InstanceType.T2Micro)
			.withMaxCount(1)
			.withMinCount(1);

		RunInstancesResult run_response = aws_ec2.runInstances(run_request);

		String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

		System.out.printf(
			"Successfully started EC2 instance %s based on AMI %s",
			reservation_id, ami_id);
	
	}

	public static void rebootInstance(String instance_id) {
		
		System.out.printf("Rebooting .... %s\n", instance_id);
		
		final AmazonEC2 aws_ec2 = AmazonEC2ClientBuilder.defaultClient();

		try {
			RebootInstancesRequest reboot_request = new RebootInstancesRequest()
					.withInstanceIds(instance_id);

				RebootInstancesResult response = aws_ec2.rebootInstances(reboot_request);

				System.out.printf(
						"Successfully rebooted instance %s", instance_id);

		} catch(Exception exp)
		{
			System.out.println("Exception: "+exp.toString());
		}

		
	}
	
	public static void listImages() {
		System.out.println("Listing images....");
		
		final AmazonEC2 aws_ec2 = AmazonEC2ClientBuilder.defaultClient();
		
		DescribeImagesRequest image_request = new DescribeImagesRequest();
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		
		image_request.getFilters().add(new Filter().withName("name").withValues("htcondor-slave-image"));
		image_request.setRequestCredentialsProvider(credentialsProvider);
		
		DescribeImagesResult results = aws_ec2.describeImages(image_request);
		
		for(Image images :results.getImages()){
			System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s\n", 
					images.getImageId(), images.getName(), images.getOwnerId());
		}
		
	}
}
	