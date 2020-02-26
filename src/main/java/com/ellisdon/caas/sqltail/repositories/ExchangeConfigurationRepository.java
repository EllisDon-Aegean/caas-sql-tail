package com.ellisdon.caas.sqltail.repositories;

import com.ellisdon.caas.sqltail.domain.ExchangeConfiguration;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeConfigurationRepository extends PagingAndSortingRepository<ExchangeConfiguration, String> {
}
