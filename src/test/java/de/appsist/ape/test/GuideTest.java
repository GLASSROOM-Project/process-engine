package de.glassroom.gpe.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import de.glassroom.gpe.Branch;
import de.glassroom.gpe.Condition;
import de.glassroom.gpe.EqualsCondition;
import de.glassroom.gpe.Guide;
import de.glassroom.gpe.GuideEnd;
import de.glassroom.gpe.GuideManager;
import de.glassroom.gpe.Node;
import de.glassroom.gpe.Step;
import de.glassroom.gpe.annotations.ContentAnnotation;
import de.glassroom.gpe.annotations.MetadataAnnotation;
import de.glassroom.gpe.content.ContentDescriptor;
import de.glassroom.gpe.utils.ContentSerializer;
import de.glassroom.gpe.utils.GuideSerializer;

public class GuideTest {
	private static int stepNo = 0;
	
	// private static String testString = "<process xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" isExecutable=\"true\" id=\"42c322b8-9c3c-4fb8-aa5b-8cd176c1c61a\">  <extensionElements>    <metadata xmlns=\"glassroom:bpmn:metadata\">      <title lang=\"de_DE\">Mutter Prozess</title>      <description lang=\"de_DE\">Einbetten von Prozessen</description>    </metadata>  </extensionElements>  <startEvent isInterrupting=\"true\" parallelMultiple=\"false\" id=\"startEvent-1577618720c\" name=\"Start Event\">    <outgoing>flow-startEvent-1577618720c_userTask-15776191a9d</outgoing>  </startEvent>  <sequenceFlow id=\"flow-startEvent-1577618720c_userTask-15776191a9d\" sourceRef=\"startEvent-1577618720c\" targetRef=\"userTask-15776191a9d\" />  <endEvent id=\"endEvent-1577618720d\" name=\"End Event\">    <incoming>flow-callActivity-15776195705_endEvent-1577618720d</incoming>  </endEvent>  <userTask completionQuantity=\"1\" implementation=\"##unspecified\" isForCompensation=\"false\" startQuantity=\"1\" id=\"userTask-15776191a9d\">    <extensionElements>      <content xmlns=\"glassroom:bpmn:content\">        <assistance>          <package lang=\"de_DE\">e0f81355-eda9-4fc0-b1fe-b91611b13a63</package>        </assistance>      </content>    </extensionElements>    <incoming>flow-startEvent-1577618720c_userTask-15776191a9d</incoming>    <outgoing>flow-userTask-15776191a9d_callActivity-15776195705</outgoing>  </userTask>  <sequenceFlow id=\"flow-userTask-15776191a9d_callActivity-15776195705\" sourceRef=\"userTask-15776191a9d\" targetRef=\"callActivity-15776195705\" />  <callActivity completionQuantity=\"1\" isForCompensation=\"false\" startQuantity=\"1\" id=\"callActivity-15776195705\" calledElement=\"0fd796df-5409-4a7f-9596-575a942d7ed9\">    <incoming>flow-userTask-15776191a9d_callActivity-15776195705</incoming>    <outgoing>flow-callActivity-15776195705_endEvent-1577618720d</outgoing>  </callActivity>  <sequenceFlow id=\"flow-callActivity-15776195705_endEvent-1577618720d\" sourceRef=\"callActivity-15776195705\" targetRef=\"endEvent-1577618720d\" /></process>";
	private static File guidesDir = new File("C:/TEMP/Ablage/2016-10-14/glassroom/guides");
	
	public static Step createStep() {
		Step step = new Step().setName("Step " + stepNo++);
		return step;
	}
	
	public static Guide reload(Guide guide) {
		String serializedGuide = GuideSerializer.writeAsBPMN(guide, false);
		return GuideSerializer.readFromBPMN(serializedGuide);
	}
	
	public static void testContinueGuide() {
		GuideManager gm = new GuideManager();
		Guide guide = gm.createGuide("guide1");
		guide.setTitle("de_DE", "Erfolge feiern.");
		guide.setDescription("de_DE", "Wie man einen Erfolg angemessen feiert.");
		guide.addNode(createStep());
		guide = reload(guide);
		guide.addNode(createStep());
		guide.addNode(createStep());
		guide = reload(guide);
		
		System.out.println(GuideSerializer.writeAsBPMN(guide, false));
	}
	
	public static void testSerializeGuide() {
		GuideManager gm = new GuideManager();
		for (Guide guide : importGuides()) {
			gm.addGuide(guide);
			System.out.println("Added guide " + guide.getId() + " with " + guide.getNodes().size() + " nodes.");
		}
		String selectedGuideId = "edfc5f60-a269-4870-a39d-fb6b93ea68d5";
		List<Step> serializedGuide = gm.serializeGuide(selectedGuideId);
		for (Step step : serializedGuide) {
			String contentId = step.getContent().getContentPackage("de_DE");
			ContentDescriptor content = readContentDescriptor(step.getParentGuide().getId(), contentId);
			System.out.println("Step found: " + content.getInfo());
		}
	}
	
	public static List<Guide> importGuides() {
		List<Guide> guides = new ArrayList<>();
        for (File file : guidesDir.listFiles()) {
            if (!file.isDirectory()) continue;
            File manifest = new File(file, "guide.bpmn");
            if (!manifest.exists()) continue;
            try {
                String bpmnString = readFile(manifest);
                Guide guide = GuideSerializer.readFromBPMN(bpmnString);
                guides.add(guide);
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Failed to import guide " + file.getName() + ": " + e.getMessage());
            }
        }
        return guides;
    }
	
	public static ContentDescriptor readContentDescriptor(String guideId, String packageId) {
        File contentPackageDir = new File(guidesDir.getAbsolutePath() + "/" + guideId + "/content/" + packageId);
        if (!contentPackageDir.exists()) {
            System.out.println("Content package " + packageId + " for guide " + guideId + " does not exist.");
        }
        
        File contentDescriptorFile = new File(contentPackageDir, "content.xml");
        if (!contentDescriptorFile.exists()) {
            System.out.println("Content descriptor for package " + packageId + " for guide " + guideId + " is missing.");
        }
        
        String descriptorString;
		try {
			descriptorString = readFile(contentDescriptorFile);
		} catch (IOException e) {
			System.out.println("Failed to read file: " + e.getMessage());
			return null;
		}
        ContentDescriptor descriptor = ContentSerializer.readFromXML(descriptorString);
        return descriptor;
    }

    private static String readFile(File file) throws IOException {
        Scanner scanner = new Scanner(file, "UTF-8").useDelimiter("\r\n");
        StringBuilder builder = new StringBuilder();
        while (scanner.hasNext()) {
            builder.append(scanner.next());
        }
        scanner.close();
        return builder.toString();
    }
	
	@SuppressWarnings("serial")
	public static void writeReadTest() {
		GuideManager pm = new GuideManager();
		Guide foo = pm.createGuide("foo");
		GuideEnd endNode = foo.getEndNodes().iterator().next();
		Step task = new Step()
				.setName("Find yourself")
				.setContent(new ContentAnnotation().setContentPackage("de_DE", "12345"))
				.setMetadata(new MetadataAnnotation().setTitle("de_DE", "Let's find yourself").setDescription("de_DE", "Try to find out who you are."));
		foo.addNode(task);
		foo.addNode(new Branch()
				.setName("Who are you?")
				.addDecision(endNode, new HashMap<String, String>() {{ put("de_DE","Me."); }}, new EqualsCondition("isMe", "true"))
				.addDecision(task, new HashMap<String, String>() {{ put("de_DE","Well ..."); }}, new EqualsCondition("isMe", "false")));
		String export = GuideSerializer.writeAsBPMN(foo, false);
		Guide fooCopy = GuideSerializer.readFromBPMN(export);
		String exportCopy = GuideSerializer.writeAsBPMN(fooCopy, false);
		System.out.println(export);
		System.out.println(exportCopy);
	}
        
        public static void removeStepTest() {
            GuideManager gm = new GuideManager();
            Guide guide = gm.createGuide("foo");
            Step task1 = new Step()
                    .setName("Fist Task");
            Step task2 = new Step()
                    .setName("Second Task");
            Step task3 = new Step()
                    .setName("Third Task");
            guide.addNode(task1);
            guide.addNode(task2);
            guide.addNode(task3);
            System.out.println("Guide before node removal:\n" + GuideSerializer.writeAsBPMN(guide, false));
            guide.removeNode(task2);
            System.out.println("Guide after node removal:\n" + GuideSerializer.writeAsBPMN(guide, false));
        }
        
        public static void moveStepTest() {
            GuideManager gm = new GuideManager();
            Guide guide = gm.createGuide("foo");
            Step task1 = new Step("task_1")
                    .setName("Fist Task");
            Step task2 = new Step("task_2")
                    .setName("Second Task");
            Step task3 = new Step("task_3")
                    .setName("Third Task");
            guide.addNode(task1);
            guide.addNode(task2);
            guide.addNode(task3);
            System.out.println("Guide before moving node:\n" + GuideSerializer.writeAsBPMN(guide, false));
            guide.moveNode(task1, task3);
            System.out.println("Guide after moving node:\n" + GuideSerializer.writeAsBPMN(guide, false));
        }
        
        public static void getAllPathsTest() {
            GuideManager gm = new GuideManager();
            Guide guide = gm.createGuide("foo");
            Node end = guide.getEndNodes().iterator().next();
            Step task1 = new Step("task_1")
                    .setName("First Task");
            Step task2_1 = new Step("task_2.1")
                    .setName("Greet Mario");
            Step task2_2 = new Step("task_2.2")
                    .setName("General Greeting");
            Branch branch = new Branch("branch")
                    .setName("Who are you?")
                    .addDecision(end, new HashMap<String, String>() {{ put("de_DE","It's me, Mario!"); }}, new EqualsCondition("isMario", "true"))
                    .addDecision(end, new HashMap<String, String>() {{ put("de_DE","Well ..."); }}, new EqualsCondition("isMario", "false"));
            guide.addNode(task1);
            guide.addNode(branch);
            for (List<Node> path : guide.getAllPaths()) {
                System.out.println("Path: " + path.toString());
            }
        }
	
	public static void main(String[] args) throws Exception {
            getAllPathsTest();
	}

}
