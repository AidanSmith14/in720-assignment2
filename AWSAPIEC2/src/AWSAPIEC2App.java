/*
 * Aidan Smith
 * 26/09/2016
 * Virtualisation Assignment 2
 */

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

/*
 * 
 * MUAHAHAHAHAHAH
 * FEAR THE WRATH OF JAVA TOM
 * 
 * README.md CONTAINS HOW TO RUN THE JAR FILE
 */

public class AWSAPIEC2App {
	
	static AmazonEC2 ec2;
	
	/*
	 * Main Method
	 * Calls specific method based on argument given
	 */
	public static void main(String[] args) 
	{	
		//Checks that an argument has been given at run time
		if (args.length == 0)
		{
			System.out.println("No argument given. Please enter start, stop, status or terminate");
			return;
		}
		
		//Creates ec2 client with California as the region
		ec2 = new AmazonEC2Client().withRegion(Regions.US_WEST_1);	
		DescribeInstancesResult result = FindInstance();
		
		List<Reservation> reservations = result.getReservations();
		
		//Calls StartInstance method 
		if (args[0].equals("start"))
		{		
			//Creates instance if one does not exist
			if (reservations.size() == 0) 
			{
				CreateInstance();
			}
			else
			{
				StartInstance(reservations);
			}				
		}
		
		//Calls StopInstance method
		else if (args[0].equals("stop"))
		{
			//Checks an instance exists
			if (reservations.size() == 0) 
			{
				System.out.println("Instance does not exist");
			}
			else
			{
				StopInstance(reservations);
			}	
		}
		
		//Calls StatusInstance method
		else if (args[0].equals("status"))
		{
			//Checks an instance exists
			if (reservations.size() == 0) 
			{
				System.out.println("Instance does not exist");
			}
			else
			{
				StatusInstance(reservations);
			}	
		}
		
		//Calls TerminateInstance method
		else if (args[0].equals("terminate"))
		{
			//Checks an instance exists
			if (reservations.size() == 0) 
			{
				System.out.println("Instance does not exist");
			}
			else
			{
				TerminateInstance(reservations);
			}	
		}
		//No argument given at run time
		else
		{
			System.out.println("Invalid argument given");
		}
	}
	
	/*
	 * FindInstance
	 * Looks for certain instance based on state, image-id and tag
	 */
	public static DescribeInstancesResult FindInstance()
	{
		DescribeInstancesRequest dIR = new DescribeInstancesRequest();
		
		//Sets tag values to Smith
		List<String> tagValues = new ArrayList<String>();
		tagValues.add("Smith");
		
		//Provides a list of states to filter on
		List<String> stateName = new ArrayList<String>();
		stateName.add("pending");
		stateName.add("running");
		stateName.add("shutting-down");
		stateName.add("stopping");
		stateName.add("stopped");
		
		//Sets imageId value to ami-31490d51
		List<String> imageID = new ArrayList<String>();
		imageID.add("ami-31490d51");
		
		//Adds above Lists to Filter List
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(new Filter("tag-value", tagValues));
		filters.add(new Filter("image-id", imageID));
		filters.add(new Filter("instance-state-name", stateName));
		
		dIR.setFilters(filters);
		
		//Returns instances that match above filters
		DescribeInstancesResult result = ec2.describeInstances(dIR);		
		return result;	
	}
	
	/*
	 * CreateInstance
	 * Creates an instance when one does not already exist
	 */
	public static void CreateInstance()
	{
		//Creates an instance with given imageId and type and ensures only one is created
		RunInstancesRequest runInstanceRequest = new RunInstancesRequest();
		runInstanceRequest.withImageId("ami-31490d51").withInstanceType("t2.nano").withMinCount(1).withMaxCount(1);
		RunInstancesResult runInstancesResult = ec2.runInstances(runInstanceRequest);
		
		//Receives id of newly created instance 	
		String instanceID = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();		
		ArrayList<String> resources = new ArrayList<String>();
		resources.add(instanceID);
		
		//Adds tags to newly created instance
		ArrayList<Tag> requestTags = new ArrayList<Tag>();
		requestTags.add(new Tag("Name", "Smith"));
		
		CreateTagsRequest createTagsRequest = new CreateTagsRequest().withResources(resources).withTags(requestTags);
		
		ec2.createTags(createTagsRequest);
		System.out.println("Instance created");
	}
	
	/*
	 * StartInstance
	 * Starts an already created instance, if not already running
	 */
	public static void StartInstance(List<Reservation> reservations)
	{
		InstanceState instanceState = reservations.get(0).getInstances().get(0).getState();
		
		//Checks if instance is running
		if (instanceState.getCode() == 16) 
		{
			System.out.println("Instance is already running");
			return;
		}
		
		//Starts the instance with the given id
		String instanceId = reservations.get(0).getInstances().get(0).getInstanceId();
		StartInstancesRequest startInstancesRequest = new StartInstancesRequest().withInstanceIds(instanceId);
		ec2.startInstances(startInstancesRequest);
		System.out.println("Instance started");
	}
	
	/*
	 * StopInstance
	 * Stops an instance, if not already stopped
	 */
	public static void StopInstance(List<Reservation> reservations)
	{
		InstanceState instanceState = reservations.get(0).getInstances().get(0).getState();
		
		//Checks if instance is stopped
		if (instanceState.getCode() == 80) 
		{
			System.out.println("Instance is already stopped");
			return;
		}
		
		//Stops the instance with the given id
		String instanceId = reservations.get(0).getInstances().get(0).getInstanceId();
		StopInstancesRequest stopInstancesRequest = new StopInstancesRequest().withInstanceIds(instanceId);
		ec2.stopInstances(stopInstancesRequest);
		System.out.println("Instance stopped");
	}
	
	/*
	 * StatusInstance
	 * Checks the status of an instance
	 */
	public static void StatusInstance(List<Reservation> reservations)
	{
		//Returns the status of an instance with then given id
		InstanceState instanceState = reservations.get(0).getInstances().get(0).getState();
		System.out.println("Instance State = " + instanceState.getName());
	}
	
	/*
	 * TerminateInstance
	 * Terminates an instance
	 */
	public static void TerminateInstance(List<Reservation> reservations)
	{
		//Terminates the instance with the given id
		String instanceId = reservations.get(0).getInstances().get(0).getInstanceId();
		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest().withInstanceIds(instanceId);
		ec2.terminateInstances(terminateInstancesRequest);
		System.out.println("Instance terminated");
	}

}
