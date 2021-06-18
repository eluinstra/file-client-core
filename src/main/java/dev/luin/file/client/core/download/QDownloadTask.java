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
package dev.luin.file.client.core.download;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;
import java.time.Instant;

import javax.annotation.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.sql.ColumnMetadata;

import dev.luin.file.client.core.Retries;
import dev.luin.file.client.core.ScheduleTime;
import dev.luin.file.client.core.download.DownloadStatus.Status;
import dev.luin.file.client.core.file.FileId;
import dev.luin.file.client.core.file.QFile;
import dev.luin.file.client.core.file.Timestamp;
import dev.luin.file.client.core.file.Url;




/**
 * QDownloadTask is a Querydsl query type for QDownloadTask
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDownloadTask extends com.querydsl.sql.RelationalPathBase<QDownloadTask> {

    private static final long serialVersionUID = 600040837;

    public static final QDownloadTask downloadTask = new QDownloadTask("download_task");

    public final DateTimePath<Instant> endDate = createDateTime("endDate", Instant.class);

    public final SimplePath<FileId> fileId = createSimple("fileId", FileId.class);

    public final SimplePath<Retries> retries = createSimple("retries", Retries.class);

    public final SimplePath<ScheduleTime> scheduleTime = createSimple("scheduleTime", ScheduleTime.class);

    public final DateTimePath<Instant> startDate = createDateTime("startDate", Instant.class);

    public final EnumPath<Status> status = createEnum("status", Status.class);

    public final DateTimePath<Instant> statusTime = createDateTime("status_time", Instant.class);

    public final SimplePath<Timestamp> timestamp = createSimple("time_stamp", Timestamp.class);

    public final SimplePath<Url> url = createSimple("url",Url.class);

    public final com.querydsl.sql.ForeignKey<QFile> sysFk10167 = createForeignKey(fileId, "id");

    public QDownloadTask(String variable) {
        super(QDownloadTask.class, forVariable(variable), "PUBLIC", "download_task");
        addMetadata();
    }

    public QDownloadTask(String variable, String schema, String table) {
        super(QDownloadTask.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDownloadTask(String variable, String schema) {
        super(QDownloadTask.class, forVariable(variable), schema, "download_task");
        addMetadata();
    }

    public QDownloadTask(Path<? extends QDownloadTask> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "download_task");
        addMetadata();
    }

    public QDownloadTask(PathMetadata metadata) {
        super(QDownloadTask.class, metadata, "PUBLIC", "download_task");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(endDate, ColumnMetadata.named("end_date").withIndex(4).ofType(Types.TIMESTAMP).withSize(26));
        addMetadata(fileId, ColumnMetadata.named("file_id").withIndex(1).ofType(Types.INTEGER).withSize(32));
        addMetadata(retries, ColumnMetadata.named("retries").withIndex(9).ofType(Types.TINYINT).withSize(8).notNull());
        addMetadata(scheduleTime, ColumnMetadata.named("schedule_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(26).notNull());
        addMetadata(startDate, ColumnMetadata.named("start_date").withIndex(3).ofType(Types.TIMESTAMP).withSize(26));
        addMetadata(status, ColumnMetadata.named("status").withIndex(6).ofType(Types.TINYINT).withSize(8));
        addMetadata(statusTime, ColumnMetadata.named("status_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(26).notNull());
        addMetadata(timestamp, ColumnMetadata.named("time_stamp").withIndex(5).ofType(Types.TIMESTAMP).withSize(26).notNull());
        addMetadata(url, ColumnMetadata.named("url").withIndex(2).ofType(Types.VARCHAR).withSize(256).notNull());
    }

}

