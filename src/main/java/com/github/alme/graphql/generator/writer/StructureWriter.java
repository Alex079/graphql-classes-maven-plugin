package com.github.alme.graphql.generator.writer;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;

public interface StructureWriter {

    void write(GqlContext context, GqlConfiguration configuration);

}
