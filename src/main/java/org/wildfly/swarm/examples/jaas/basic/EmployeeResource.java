package org.wildfly.swarm.examples.jaas.basic;

import javax.enterprise.context.RequestScoped;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static java.util.logging.Level.SEVERE;

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
    public Response getAll() {
        return Response.ok().
                entity(em.createQuery("SELECT s FROM Employee s", Employee.class).getResultList()).build();
    }

    @GET
    @Path("addNewEmp")
    @Transactional
    public Response addEmp() {
        try {
            UserTransaction transaction = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
            transaction.begin();
            Employee newEmp = new Employee();
            newEmp.setName("Anwar");
            newEmp.setPassword("0000");
            newEmp.setRole("user");
            em.persist(newEmp);
            transaction.commit();
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().
                    entity(e).
                    build();
        }
    }

    @GET
    @Path("admin/{id}")
    @Transactional
    public Response modifyEmp(@PathParam("id") int id) {
        try {
            //em.joinTransaction();
            return Response.ok().
                    entity(em.createQuery("UPDATE Employee e SET e.name = 'XY1234' WHERE e.id = :id").setParameter("id", id).executeUpdate()).
                    build();
        } catch (Exception e) {
            return Response.serverError().
                    entity(e).
                    build();
        }
    }

    @GET
    @Path("admin/delete/{id}")
    @Transactional
    public Response deleteEmp(@PathParam("id") int id) {
        try {
            //em.joinTransaction();
            return Response.ok().
                    entity(em.createQuery("DELETE FROM Employee e WHERE e.id = :id").setParameter("id", id)).
                    build();
        } catch (Exception e) {
            return Response.serverError().
                    entity(e).
                    build();
        }
    }
}
