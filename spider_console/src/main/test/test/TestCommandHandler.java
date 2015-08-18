package test;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler.ConsoleCommand;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.junit.Test;

public class TestCommandHandler {

	@Test
	public void testHandler(){
		System.err.println(ConsoleCommandManager.getManager().toString());
	}

}
