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
    public IngestDocument execute(IngestDocument ingestDocument) throws Exception {
        CommunityIdGenerator generator = new CommunityIdGenerator();
        String sourceIp = ingestDocument.getFieldValue(fields.get(0), String.class);
        String sourcePort = ingestDocument.getFieldValue(fields.get(1), String.class);
        String destinationIp = ingestDocument.getFieldValue(fields.get(2), String.class);
        String destinationPort = ingestDocument.getFieldValue(fields.get(3), String.class);

        String result = generator.generateCommunityId(Protocol.UDP,
                InetAddress.getByName(sourceIp), Integer.parseInt(sourcePort),
                InetAddress.getByName(destinationIp), Integer.parseInt(destinationPort));
        ingestDocument.setFieldValue(targetField, result);
        return ingestDocument;
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
                fields.add((String) field);
            }

            String targetField = readStringProperty(TYPE, tag, config, "target_field", "default_field_name");

            return new CommunityIdProcessor(tag, fields, targetField);
        }
    }
}