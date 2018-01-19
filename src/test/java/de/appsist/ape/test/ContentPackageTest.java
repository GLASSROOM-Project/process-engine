package de.glassroom.gpe.test;

import java.util.Date;

import de.glassroom.gpe.content.ContentDescriptor;
import de.glassroom.gpe.content.Hint;
import de.glassroom.gpe.content.Warning;
import de.glassroom.gpe.utils.ContentSerializer;

public class ContentPackageTest {
	
	public static void main(String[] args) throws Exception {
		ContentDescriptor descriptor = new ContentDescriptor("myId", "de_DE");
		descriptor.setInfo("Lorem ipsum.");
		descriptor.setTitle("My title");
		descriptor.setLastUpdate(new Date());
		descriptor.setMedia("image/jpeg", "images/foo.jpg");
		descriptor.setRoutineTask(true);
		descriptor.setVersion("1.2");
		descriptor.addHint(new Hint("My nice hint for you."));
		descriptor.addWarning(new Warning("Beware!", "images/alert.png"));
		
		String serializedDescriptor = ContentSerializer.writeAsJSON(descriptor);
		
		System.out.print(serializedDescriptor);
		
		descriptor = ContentSerializer.readFromJSON(serializedDescriptor);
		
		System.out.println(ContentSerializer.writeAsJSON(descriptor));
	}

}
