package org.epiclouds.spiders.command.impl;

import java.io.IOException;
import java.util.List;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.spiderbean.util.SpiderClassBean;
import org.epiclouds.spiders.spiderbean.util.SpiderObjectBean;
import org.epiclouds.spiders.spiderbean.util.SpiderStatusManager;
import org.epiclouds.spiders.util.ConsoleConfig;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;
/**
 * The start command handler
 * @author xianglong
 *
 */
@Component
public class GetSpiderObjectHandlerImpl extends  AbstractConsoleCommandHandler{

	public GetSpiderObjectHandlerImpl() {
		super(ConsoleCommand.GETSTATE);
	}

}
