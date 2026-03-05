package com.personal.store_api.repository;

import com.personal.store_api.document.ProductDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    Page<ProductDocument> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<ProductDocument> findByNameStartingWithIgnoreCase(String prefix);

    @Query("""
        {
          "bool": {
            "must": [
              {
                "multi_match": {
                  "query": "?0",
                  "fields": ["name^2", "categoryName", "brandName"],
                  "type": "best_fields",
                  "fuzziness": "AUTO"
                }
              }
            ],
            "filter": [
              {
                "range": {
                  "price": {
                    "gte": ?1,
                    "lte": ?2
                  }
                }
              }
            ]
          }
        }
        """)
    Page<ProductDocument> searchProducts(String query, Double minPrice, Double maxPrice, Pageable pageable);

    @Query("""
        {
          "bool": {
            "must": [
              {
                "multi_match": {
                  "query": "?0",
                  "fields": ["name^2", "categoryName", "brandName"],
                  "type": "best_fields",
                  "fuzziness": "AUTO"
                }
              }
            ],
            "filter": [
              {
                "range": {
                  "price": {
                    "gte": ?1,
                    "lte": ?2
                  }
                }
              },
              {
                "term": {
                  "brandName.keyword": ?3
                }
              }
            ]
          }
        }
        """)
    Page<ProductDocument> searchProducts(String query, Double minPrice, Double maxPrice, String brandName, Pageable pageable);

    @Query("""
        {
          "bool": {
            "must": [
              {
                "multi_match": {
                  "query": "?0",
                  "fields": ["name^2", "categoryName", "brandName"],
                  "type": "best_fields",
                  "fuzziness": "AUTO"
                }
              }
            ],
            "filter": [
              {
                "range": {
                  "price": {
                    "gte": ?1,
                    "lte": ?2
                  }
                }
              },
              {
                "term": {
                  "categoryName.keyword": ?3
                }
              }
            ]
          }
        }
        """)
    Page<ProductDocument> searchProductsByCategory(String query, Double minPrice, Double maxPrice, String categoryName, Pageable pageable);

    @Query("""
        {
          "bool": {
            "must": [
              {
                "multi_match": {
                  "query": "?0",
                  "fields": ["name^2", "categoryName", "brandName"],
                  "type": "best_fields",
                  "fuzziness": "AUTO"
                }
              }
            ],
            "filter": [
              {
                "range": {
                  "price": {
                    "gte": ?1,
                    "lte": ?2
                  }
                }
              },
              {
                "term": {
                  "brandName.keyword": ?3
                }
              },
              {
                "term": {
                  "categoryName.keyword": ?4
                }
              }
            ]
          }
        }
        """)
    Page<ProductDocument> searchProducts(String query, Double minPrice, Double maxPrice, String brandName, String categoryName, Pageable pageable);
}
