/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/04/16 i18n
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.

package org.parosproxy.paros.extension.filter;

import java.util.regex.Matcher;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FilterReplaceRequestBody extends FilterAbstractReplace {

    @Override
    public int getId() {
        return 60;
    }

    @Override
    public String getName() {
        return Constant.messages.getString("filter.replacereqbody.name");
    }
    @Override
    public void onHttpRequestSend(HttpMessage msg) {

        if (getPattern() == null) {
            return;
        } else if (msg.getRequestHeader().isEmpty() || msg.getRequestBody().length() == 0) {
            return;
        }
        
        Matcher matcher = getPattern().matcher(msg.getRequestBody().toString());
        String result = matcher.replaceAll(getReplaceText());
        msg.getRequestBody().setBody(result);
        msg.getRequestHeader().setContentLength(msg.getRequestBody().length());
                   
    }

    @Override
    public void onHttpResponseReceive(HttpMessage msg) {
 
    } 
}