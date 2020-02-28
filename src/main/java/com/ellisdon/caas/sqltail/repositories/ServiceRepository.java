package com.ellisdon.caas.sqltail.repositories;

import com.ellisdon.caas.sqltail.domain.Service;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends PagingAndSortingRepository<Service, String> {
}
