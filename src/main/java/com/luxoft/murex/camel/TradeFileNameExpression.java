/**
 *  Copyright Murex S.A.S., 2003-2019. All Rights Reserved.
 *
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package com.luxoft.murex.camel;

import com.luxoft.murex.camel.model.Trade;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;


public class TradeFileNameExpression implements Expression {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods
    //~ ----------------------------------------------------------------------------------------------------------------

    @Override
    public <T> T evaluate(Exchange exchange, Class<T> type) {
        Trade trade = exchange.getIn().getBody(Trade.class);
        String name = trade.getCurrency() + "-" + trade.getCounterParty() + ".xml";
        return exchange.getContext().getTypeConverter().convertTo(type, name);
    }
}
