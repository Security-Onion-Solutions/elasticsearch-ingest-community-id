/*
 * Copyright [2018] [Ronak Gothi]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.elasticsearch.plugin.ingest.community.id;

import com.rapid7.communityid.CommunityIdGenerator;
import com.rapid7.communityid.Protocol;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.net.InetAddress;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readObject;

public class CommunityIdProcessor extends AbstractProcessor {

    public static final String TYPE = "community_id";

    private final List<String> fields;
    private final String targetField;

    public CommunityIdProcessor(String tag, List<String> field, String targetField) throws IOException {
        super(tag);
        this.fields = new ArrayList<>(field);
        this.targetField = targetField;
    }

    @Override
    public IngestDocument execute(IngestDocument document) throws Exception {
        CommunityIdGenerator generator = new CommunityIdGenerator();

        if (document.hasField(fields.get(0), true) == false ||
            document.hasField(fields.get(1), true) == false ||
            document.hasField(fields.get(2), true) == false ||
            document.hasField(fields.get(3), true) == false ||
            document.hasField(fields.get(4), true) == false
        ) {
            return document;
        }

        String sourceIp = document.getFieldValue(fields.get(0), String.class);
        Integer sourcePort = getPort(fields.get(1), document);
        String destinationIp = document.getFieldValue(fields.get(2), String.class);
        Integer destinationPort = getPort(fields.get(3), document);
        String protocol = document.getFieldValue(fields.get(4), String.class);

        String result = generator.generateCommunityId(getProtocol(protocol),
                InetAddress.getByName(sourceIp), sourcePort,
                InetAddress.getByName(destinationIp), destinationPort);
        document.setFieldValue(targetField, result);
        return document;
    }

    private Protocol getProtocol(String protocol) {
        if (protocol.toLowerCase(Locale.ROOT).equals("tcp"))
            return Protocol.TCP;
        else if (protocol.toLowerCase(Locale.ROOT).equals("udp"))
            return Protocol.UDP;
        else if (protocol.toLowerCase(Locale.ROOT).equals("sctp"))
            return Protocol.SCTP;
        else
            return Protocol.TCP;
    }

    private Integer getPort(String portField, IngestDocument document) {
        Integer port;
        try {
            port = document.getFieldValue(portField, Integer.class);
        } catch (Exception e) {
            String temp = document.getFieldValue(portField, String.class);
            port = Integer.parseInt(temp);
        }
        return port;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory implements Processor.Factory {
        @Override
        public CommunityIdProcessor create(Map<String, Processor.Factory> factories, String tag, Map<String, Object> config)
                throws Exception {
            final List<String> fields = new ArrayList<>();
            final Object field = readObject(TYPE, tag, config, "field");

            if (field instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> stringList = (List<String>) field;
                fields.addAll(stringList);
            } else {
                throw new IllegalArgumentException("field should be an Array");
            }

            String targetField = readStringProperty(TYPE, tag, config, "target_field", "default_field_name");

            return new CommunityIdProcessor(tag, fields, targetField);
        }
    }
}