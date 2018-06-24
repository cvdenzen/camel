/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.pilight;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.MultipleConsumersSupport;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.util.ObjectHelper;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoAcceptorConfig;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoConnectorConfig;
import org.apache.mina.common.IoSession;

/**
 * Socket level networking using TCP or UDP with the Apache Pilight 1.x library.
 */
@UriEndpoint(firstVersion = "1.0.0", scheme = "mina", title = "Pilight", syntax = "mina:protocol:host:port", consumerClass = PilightConsumer.class, label = "networking,tcp,udp")
public class PilightEndpoint extends DefaultEndpoint implements MultipleConsumersSupport {

    /** The key of the IoSession which is stored in the message header*/
    @Deprecated
    public static final String HEADER_MINA_IOSESSION = "CamelPilightIoSession";
    /** The socket address of local machine that received the message. */
    @Deprecated
    public static final String HEADER_LOCAL_ADDRESS = "CamelPilightLocalAddress";
    /** The socket address of the remote machine that send the message. */
    @Deprecated
    public static final String HEADER_REMOTE_ADDRESS = "CamelPilightRemoteAddress";

    private SocketAddress address;
    private IoAcceptor acceptor;
    private IoConnector connector;
    private IoAcceptorConfig acceptorConfig;
    private IoConnectorConfig connectorConfig;
    @UriParam
    private PilightConfiguration configuration;
    private final List<ExecutorService> executors = new ArrayList<>();

    public PilightEndpoint() {
    }

    public PilightEndpoint(String endpointUri, PilightComponent component) {
        super(endpointUri, component);
    }

    public Producer createProducer() throws Exception {
        ObjectHelper.notNull(configuration, "configuration"); 
        ObjectHelper.notNull(address, "address");
        ObjectHelper.notNull(connector, "connector");
        // wm protocol does not have config
        if (!configuration.getProtocol().equalsIgnoreCase("vm")) {
            ObjectHelper.notNull(connectorConfig, "connectorConfig");
        }
        return new PilightProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        ObjectHelper.notNull(configuration, "configuration");
        ObjectHelper.notNull(address, "address");
        ObjectHelper.notNull(acceptor, "acceptor");
        // wm protocol does not have config
        if (!configuration.getProtocol().equalsIgnoreCase("vm")) {
            ObjectHelper.notNull(acceptorConfig, "acceptorConfig");
        }
        PilightConsumer answer = new PilightConsumer(this, processor);
        configureConsumer(answer);
        return answer;
    }

    public Exchange createExchange(IoSession session, Object payload) {
        Exchange exchange = createExchange();
        exchange.getIn().setHeader(PilightConstants.MINA_IOSESSION, session);
        exchange.getIn().setHeader(PilightConstants.MINA_LOCAL_ADDRESS, session.getLocalAddress());
        exchange.getIn().setHeader(PilightConstants.MINA_REMOTE_ADDRESS, session.getRemoteAddress());
        PilightPayloadHelper.setIn(exchange, payload);
        return exchange;
    }

    public boolean isSingleton() {
        return true;
    }

    public boolean isMultipleConsumersSupported() {
        // only datagram should allow multiple consumers
        return configuration.isDatagramProtocol();
    }

    @Override
    protected void doShutdown() throws Exception {
        // shutdown thread pools
        for (ExecutorService executor : executors) {
            getCamelContext().getExecutorServiceManager().shutdownNow(executor);
        }
        executors.clear();
        super.doShutdown();
    }

    /**
     * Add thread pool which are in-use, we need to un-register when shutting down.
     */
    protected void addThreadPool(ExecutorService executorService) {
        executors.add(executorService);
    }

    // Properties
    // -------------------------------------------------------------------------

    public PilightConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(PilightConfiguration configuration) {
        this.configuration = configuration;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void setAddress(SocketAddress address) {
        this.address = address;
    }

    public IoAcceptor getAcceptor() {
        return acceptor;
    }

    public void setAcceptor(IoAcceptor acceptor) {
        this.acceptor = acceptor;
    }

    public IoConnector getConnector() {
        return connector;
    }

    public void setConnector(IoConnector connector) {
        this.connector = connector;
    }

    public IoAcceptorConfig getAcceptorConfig() {
        return acceptorConfig;
    }

    public void setAcceptorConfig(IoAcceptorConfig acceptorConfig) {
        this.acceptorConfig = acceptorConfig;
    }

    public IoConnectorConfig getConnectorConfig() {
        return connectorConfig;
    }

    public void setConnectorConfig(IoConnectorConfig connectorConfig) {
        this.connectorConfig = connectorConfig;
    }

}
