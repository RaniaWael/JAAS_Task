package org.wildfly.swarm.examples.jaas.basic;

import org.springframework.test.annotation.Rollback;

import javax.enterprise.context.RequestScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * @author Ken Finnigan
 */
@Path("/")
@RequestScoped
public class EmployeeResource {

    @PersistenceContext
    private EntityManager em;

    @Context
    private SecurityContext securityContext;

    @GET
    @Produces("application/json")
    public Employee[] get() {
        return securityContext.isUserInRole("admin")
                ? em.createNamedQuery("Employee.findAll", Employee.class).getResultList().toArray(new Employee[0])
                : new Employee[0];
    }

    @GET
    @Path("getAllEmployees")
    public Employee[] getAll() {
        return em.createNamedQuery("Employee.findAll", Employee.class).getResultList().toArray(new Employee[0]);
    }

    @GET
    @Path("addNewEmp")
    public Employee[] addEmp() throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException, NamingException {
        Employee newEmp = new Employee();
        newEmp.setName("Anwar");
        newEmp.setPassword("0000");
        newEmp.setRole("user");
//        UserTransaction transaction = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
//        transaction.begin();
//        em.getTransaction().begin();
//        em.createNativeQuery("INSERT INTO USERS (name, password, role) VALUES ('Anwar', '0000', 'user')").executeUpdate();
        em.persist(newEmp);
//        em.close();
//        em.getTransaction().commit();
//        transaction.commit();
        return em.createNamedQuery("Employee.findAll", Employee.class).getResultList().toArray(new Employee[0]);
    }

    @GET
    @Path("admin/{id}")
    @Transactional
    public Employee[] modifyEmp(@PathParam("id") int id) {
        try {
            em.createQuery("UPDATE Employee e SET e.name = 'Mariam' WHERE e.id = :id").setParameter("id", id).executeUpdate();
            return em.createNamedQuery("Employee.findAll", Employee.class).getResultList().toArray(new Employee[0]);
        } catch (Exception e) {
            return new Employee[0];
        }
    }

    @GET
    @Path("admin/delete/{id}")
    @Transactional
    @Rollback(true)
    public Employee[] deleteEmp(@PathParam("id") int id) {
        try {

            Employee employee = em.find(Employee.class, id);
            em.remove(employee);

            return em.createNamedQuery("Employee.findAll", Employee.class).getResultList().toArray(new Employee[0]);
        } catch (Exception e) {
            return new Employee[0];
        }
    }
}
