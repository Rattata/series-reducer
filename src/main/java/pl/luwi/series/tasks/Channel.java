package pl.luwi.series.tasks;

import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

public class Channel<T, E> {
	
//	jms destination takes precedence over
	
	LinkedBlockingQueue<E> queue;
	
	String destination;
	MessageProducer producer;
	MessageConsumer consumer;
	Session session;
	
	
	
	public void put(T t){
		if(queue == null){
			producer.send(session.create);
		}
	};
	
	public T poll(T t){
		return null;
	}; 
}
