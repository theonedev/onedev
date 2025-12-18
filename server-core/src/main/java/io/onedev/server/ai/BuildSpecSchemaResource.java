package io.onedev.server.ai;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.onedev.server.rest.annotation.Api;

@Api(internal = true)
@Path("/build-spec-schema.yml")
@Consumes(MediaType.APPLICATION_JSON)
@Produces("application/x-yaml")
@Singleton
public class BuildSpecSchemaResource {

    @GET
    public String getBuildSpecSchema() {
        return BuildSpecSchema.get();
    }

 }