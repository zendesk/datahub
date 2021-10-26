package com.datahub.metadata.graphql;

import com.datahub.metadata.authorization.AuthorizationManager;
import com.linkedin.datahub.graphql.GmsGraphQLEngine;
import com.linkedin.datahub.graphql.GraphQLEngine;
import com.linkedin.datahub.graphql.analytics.service.AnalyticsService;
import com.linkedin.entity.client.JavaEntityClient;
import com.linkedin.gms.factory.common.IndexConventionFactory;
import com.linkedin.gms.factory.common.RestHighLevelClientFactory;
import com.linkedin.metadata.entity.EntityService;
import com.linkedin.metadata.graph.GraphClient;
import com.linkedin.metadata.utils.elasticsearch.IndexConvention;
import javax.annotation.Nonnull;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;

@Configuration
@Import({RestHighLevelClientFactory.class, IndexConventionFactory.class, RestliEntityClientFactory.class, AuthorizationManagerFactory.class})
public class GraphQLEngineFactory {
  @Autowired
  @Qualifier("elasticSearchRestHighLevelClient")
  private RestHighLevelClient elasticClient;

  @Autowired
  @Qualifier(IndexConventionFactory.INDEX_CONVENTION_BEAN)
  private IndexConvention indexConvention;

  @Autowired
  @Qualifier("javaEntityClient")
  private JavaEntityClient _entityClient;

  @Autowired
  @Qualifier("entityService")
  private EntityService _entityService;

  @Autowired
  @Qualifier("graphClient")
  private GraphClient _graphClient;

  @Autowired
  private AuthorizationManager authorizationManager;

  @Value("${ANALYTICS_ENABLED:true}") // TODO: Migrate to DATAHUB_ANALYTICS_ENABLED
  private Boolean isAnalyticsEnabled;

  @Bean(name = "graphQLEngine")
  @Nonnull
  protected GraphQLEngine getInstance() {
    if (isAnalyticsEnabled) {
      return new GmsGraphQLEngine(
          new AnalyticsService(elasticClient, indexConvention.getPrefix()), _entityService, _graphClient, _entityClient
      ).builder().build();
    }
    return new GmsGraphQLEngine(null, _entityService, _graphClient, _entityClient).builder().build();
  }
}