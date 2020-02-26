package com.ellisdon.caas.sqltail.repositories;

import com.ellisdon.caas.sqltail.domain.Feature;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureRepository extends PagingAndSortingRepository<Feature, String> {
}
