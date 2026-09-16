package io.onedev.server.ai;

import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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