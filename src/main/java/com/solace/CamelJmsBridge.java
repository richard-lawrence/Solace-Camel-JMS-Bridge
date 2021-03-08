package com.solace;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.jms.JMSException;

import com.solacesystems.jms.SupportedProperty;

import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.builder.RouteBuilder;

public class CamelJmsBridge
{

    private static final String SOLJMS_INITIAL_CONTEXT_FACTORY = 
        "com.solacesystems.jndi.SolJNDIInitialContextFactory"; 

    // The default JNDI name of the connection factory.
    private static String cfJNDIName = "/jms/cf/default";
    
    public static void main(String[] args)
    {
	try {

	    // Create from JMS Connection Factory
	    Hashtable<String, Object> fromEnv = new Hashtable<String, Object>();
	    fromEnv.put(InitialContext.INITIAL_CONTEXT_FACTORY, SOLJMS_INITIAL_CONTEXT_FACTORY);
	    fromEnv.put(InitialContext.PROVIDER_URL, "tcp://localhost:55555");
	    fromEnv.put(Context.SECURITY_PRINCIPAL, "default");
	    fromEnv.put(Context.SECURITY_CREDENTIALS, "default"); 
	    fromEnv.put(SupportedProperty.SOLACE_JMS_VPN, "default");
	    fromEnv.put(SupportedProperty.SOLACE_JMS_SSL_VALIDATE_CERTIFICATE, false);  // enables the use of smfs://  without specifying a trust store

	    InitialContext fromInitialContext = new InitialContext(fromEnv);
            ConnectionFactory fromCF = (ConnectionFactory)fromInitialContext.lookup(cfJNDIName);

	    // Create to JMS Connection Factory
	    Hashtable<String, Object> toEnv = new Hashtable<String, Object>();
	    toEnv.put(InitialContext.INITIAL_CONTEXT_FACTORY, SOLJMS_INITIAL_CONTEXT_FACTORY);
	    toEnv.put(InitialContext.PROVIDER_URL, "tcp://xxx.messaging.solace.cloud:55555");
	    toEnv.put(Context.SECURITY_PRINCIPAL, "solace-cloud-client");
	    toEnv.put(Context.SECURITY_CREDENTIALS, "xxx"); 
	    toEnv.put(SupportedProperty.SOLACE_JMS_VPN, "xxx");
	    toEnv.put(SupportedProperty.SOLACE_JMS_SSL_VALIDATE_CERTIFICATE, false);  // enables the use of smfs://  without specifying a trust store
 
	    InitialContext toInitialContext = new InitialContext(toEnv);
            ConnectionFactory toCF = (ConnectionFactory)toInitialContext.lookup(cfJNDIName);
 
  
	    // Create Camel Context
	    CamelContext ctx = new DefaultCamelContext();

	    // Use Client Ack to ensure message is not lost if receiving queue fails
	    ctx.addComponent("from-jms", JmsComponent.jmsComponentClientAcknowledge(fromCF));
	    ctx.addComponent("to-jms", JmsComponent.jmsComponentClientAcknowledge(toCF));


	    ctx.addRoutes(new RouteBuilder()
	    {
		public void configure() {
		    from("from-jms:queue:camel-queue").to("to-jms:queue:camel-queue");
		}
	    });
	    ctx.start();
	    Thread.sleep(10 * 60 * 1000);
	    ctx.stop();
	}
	catch (NamingException e) {
            e.printStackTrace();
        }
	catch (JMSException e) {
            e.printStackTrace();
        }
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }
}
