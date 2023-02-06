package pas.au.snyk.se.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pas.au.snyk.se.domain.Customer;
import pas.au.snyk.se.domain.CustomerNotFound;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import com.vuln.lib.*;

@RestController
@RequestMapping("/v1/customers")
public class CustomerRestController {
    private CustomerRepository customerRepository;

    @Autowired
    public CustomerRestController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    ResponseEntity<?> options() {
        return ResponseEntity.ok()
                .allow(HttpMethod.DELETE,
                        HttpMethod.GET,
                        HttpMethod.HEAD,
                        HttpMethod.PUT,
                        HttpMethod.POST,
                        HttpMethod.OPTIONS).build();
    }

    @GetMapping
    ResponseEntity<Collection<Customer>> getCollection () {
        return ResponseEntity.ok(customerRepository.findAll());
    }

    @GetMapping (value = "/{id}")
    ResponseEntity<Optional<Customer>> get(@PathVariable Long id) {
        return Optional.ofNullable(customerRepository.findById(id))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new CustomerNotFound(id));
    }

    @PostMapping
    ResponseEntity<Customer> post (@RequestBody Customer c) {
        Customer customer = new Customer(c.getName(), c.getStatus());
        customerRepository.save(customer);

        URI uri;
        uri = MvcUriComponentsBuilder.fromController(getClass()).path("/{id}")
                .buildAndExpand(customer.getId()).toUri();

        return ResponseEntity.created(uri).body(customer);
    }

    @DeleteMapping (value = "/{id}")
    ResponseEntity<?> delete (@PathVariable Long id)
    {
        return Optional.ofNullable(customerRepository.findById(id))
                .map(c -> {
                    customerRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseThrow(() -> new CustomerNotFound(id));

    }

    @RequestMapping (value = "/{id}", method = RequestMethod.HEAD)
    ResponseEntity<?> head (@PathVariable Long id) {
        return Optional.ofNullable(customerRepository.findById(id))
                .map(exists -> ResponseEntity.noContent().build())
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping (value = "/{id}")
    ResponseEntity<Customer> put(@PathVariable Long id, @RequestBody Customer c) {
        return Optional.ofNullable(customerRepository.findById(id))
                .map(existing -> {
                    Customer customer = new Customer();
                    customer.setId(existing.get().getId());
                    customer.setName(c.getName());
                    customer.setStatus(c.getStatus());
                    customerRepository.save(customer);

                    URI selfLink = URI.create(ServletUriComponentsBuilder.fromCurrentRequest().toUriString());

                    return ResponseEntity.created(selfLink).body(customer);
                })
                .orElseThrow(() -> new CustomerNotFound(id));
    }

}
