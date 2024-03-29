/**
 *  Copyright Murex S.A.S., 2003-2019. All Rights Reserved.
 *
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package com.luxoft.murex.camel;

import javax.xml.bind.JAXBContext;

import com.luxoft.murex.camel.model.Trade;
import com.luxoft.murex.camel.model.Trades;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.spi.DataFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JmsExample {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers
    //~ ----------------------------------------------------------------------------------------------------------------

    private static Logger LOGGER = LoggerFactory.getLogger(JmsExample.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods
    //~ ----------------------------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addComponent("activemq", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
        DataFormat tradesJaxbContext = new JaxbDataFormat(JAXBContext.newInstance(Trade.class, Trades.class));

        //J-
        context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                    from("file:c:/webinar/input").to("activemq:test.queue");

                    from("activemq:test.queue")
                        .log("${body}")
                        .unmarshal(tradesJaxbContext)
                        .split(simple("${body.items}"))
                        .filter(exchange -> exchange.getIn().getBody(Trade.class).getAmount() > 1000)
                            .process(new RoundAmountProcessor())
                            .setHeader(DefaultExchange.FILE_NAME, new TradeFileNameExpression())
                            .choice()
                                .when(exchange -> exchange.getIn().getBody(Trade.class).getCounterParty().startsWith("broker"))
                                    .marshal(tradesJaxbContext)
                                    .log("One broker sent to endpoint")
                                    .to("file:c:/webinar/broker")
                                .otherwise()
                                    .marshal(tradesJaxbContext)
                                    .log("One trader sent to endpoint")
                                    .to("file:c:/webinar/trader")
                            .end()
                        .end()
                    .end();
                }
            });
        //J+

        runContext(context);
    }

    private static void runContext(CamelContext context) throws Exception {
        try {
            LOGGER.info("---------------------- START ----------------------");
            context.start();
            Thread.sleep(2 * 60 * 1000);
            LOGGER.info("----------------------  END  ----------------------");
        } finally {
            context.stop();
        }
    }
}
