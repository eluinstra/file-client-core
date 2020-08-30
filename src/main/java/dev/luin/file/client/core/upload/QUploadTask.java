/**
 * Copyright 2020 E.Luinstra
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.luin.file.client.core.upload;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;

import dev.luin.file.client.core.file.QFile;

import java.sql.Types;




/**
 * QUploadTask is a Querydsl query type for QUploadTask
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QUploadTask extends com.querydsl.sql.RelationalPathBase<QUploadTask> {

    private static final long serialVersionUID = -2093892482;

    public static final QUploadTask uploadTask = new QUploadTask("upload_task");

    public final SimplePath<java.net.URL> creationUrl = createSimple("creationUrl",java.net.URL.class);

    public final NumberPath<Long> fileId = createNumber("fileId", Long.class);

    public final NumberPath<Integer> retries = createNumber("retries", Integer.class);

    public final DateTimePath<java.time.Instant> scheduleTime = createDateTime("scheduleTime", java.time.Instant.class);

    public final EnumPath<dev.luin.file.client.core.upload.UploadStatus> status = createEnum("status", dev.luin.file.client.core.upload.UploadStatus.class);

    public final DateTimePath<java.time.Instant> timestamp = createDateTime("timestamp", java.time.Instant.class);

    public final com.querydsl.sql.ForeignKey<QFile> sysFk10151 = createForeignKey(fileId, "id");

    public QUploadTask(String variable) {
        super(QUploadTask.class, forVariable(variable), "PUBLIC", "upload_task");
        addMetadata();
    }

    public QUploadTask(String variable, String schema, String table) {
        super(QUploadTask.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUploadTask(String variable, String schema) {
        super(QUploadTask.class, forVariable(variable), schema, "upload_task");
        addMetadata();
    }

    public QUploadTask(Path<? extends QUploadTask> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "upload_task");
        addMetadata();
    }

    public QUploadTask(PathMetadata metadata) {
        super(QUploadTask.class, metadata, "PUBLIC", "upload_task");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(creationUrl, ColumnMetadata.named("creation_url").withIndex(2).ofType(Types.VARCHAR).withSize(256).notNull());
        addMetadata(fileId, ColumnMetadata.named("file_id").withIndex(1).ofType(Types.INTEGER).withSize(32).notNull());
        addMetadata(retries, ColumnMetadata.named("retries").withIndex(6).ofType(Types.TINYINT).withSize(8).notNull());
        addMetadata(scheduleTime, ColumnMetadata.named("schedule_time").withIndex(5).ofType(Types.TIMESTAMP).withSize(26).notNull());
        addMetadata(status, ColumnMetadata.named("status").withIndex(4).ofType(Types.TINYINT).withSize(8));
        addMetadata(timestamp, ColumnMetadata.named("timestamp").withIndex(3).ofType(Types.TIMESTAMP).withSize(26).notNull());
    }

}

