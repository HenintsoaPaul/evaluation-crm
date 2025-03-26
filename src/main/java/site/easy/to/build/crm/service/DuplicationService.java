package site.easy.to.build.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.repository.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DuplicationService {

    private final CustomerRepository customerRepository;
    private final CustomerLoginInfoRepository customerLoginInfoRepository;
    private final TicketRepository ticketRepository;
    private final LeadRepository leadRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public void duplicate(Customer original) {
        Customer clone = cloneCustomer(original);
        customerRepository.save(clone);

        CustomerLoginInfo cliClone = cloneCustomerLoginInfo(clone);
        customerLoginInfoRepository.save(cliClone);

        clone.setCustomerLoginInfo(cliClone);
        customerRepository.save(clone);

        cloneLeads(original, clone);
        cloneTickets(original, clone);
    }

    private void cloneLeads(Customer original, Customer clone) {
        int customerId = original.getCustomerId();
        List<Lead> leads = leadRepository.findByCustomerCustomerId(customerId),
                leadClones = new ArrayList<>();

        for (Lead leadOrg : leads) {
            Lead leadClone = new Lead();
            leadClone.setName(leadOrg.getName());
            leadClone.setStatus(leadOrg.getStatus());
            leadClone.setPhone(leadOrg.getPhone());
            leadClone.setMeetingId(leadOrg.getMeetingId());
            leadClone.setGoogleDrive(leadOrg.getGoogleDrive());
            leadClone.setGoogleDriveFolderId(leadOrg.getGoogleDriveFolderId());
            leadClone.setLeadActions(leadOrg.getLeadActions());
            leadClone.setFiles(leadOrg.getFiles());
            leadClone.setGoogleDriveFiles(leadOrg.getGoogleDriveFiles());
            leadClone.setManager(leadOrg.getManager());
            leadClone.setEmployee(leadOrg.getEmployee());
            leadClone.setCreatedAt(leadOrg.getCreatedAt());

            // exp
            Expense expOriginal = expenseRepository.findByLeadId(leadOrg.getLeadId());
            Expense expClone = new Expense();
            expClone.setAmount(expOriginal.getAmount());
            expClone.setCreationDate(expOriginal.getCreationDate());
            expClone.setLead(leadClone);
            expenseRepository.save(expClone);

            leadClone.setCustomer(clone);
            leadClone.setExpense(expClone);
            leadClones.add(leadClone);
        }
        leadRepository.saveAll(leadClones);
    }

    private void cloneTickets(Customer original, Customer clone) {
        int customerId = original.getCustomerId();
        List<Ticket> tickets = ticketRepository.findByCustomerCustomerId(customerId),
                ticketClones = new ArrayList<>();

        for (Ticket ticketOrg : tickets) {
            Ticket ticketClone = new Ticket();
            ticketClone.setSubject(ticketOrg.getSubject());
            ticketClone.setDescription(ticketOrg.getDescription());
            ticketClone.setStatus(ticketOrg.getStatus());
            ticketClone.setPriority(ticketOrg.getPriority());
            ticketClone.setManager(ticketOrg.getManager());
            ticketClone.setEmployee(ticketOrg.getEmployee());
            ticketClone.setCreatedAt(ticketOrg.getCreatedAt());

            // exp
            Expense expOriginal = expenseRepository.findByLeadId(ticketOrg.getTicketId());
            Expense expClone = new Expense();
            expClone.setAmount(expOriginal.getAmount());
            expClone.setCreationDate(expOriginal.getCreationDate());
            expClone.setTicket(ticketClone);
            expenseRepository.save(expClone);

            ticketClone.setCustomer(clone);
            ticketClone.setExpense(expClone);
            ticketClones.add(ticketClone);
        }
        ticketRepository.saveAll(ticketClones);
    }

    private CustomerLoginInfo cloneCustomerLoginInfo(Customer cloneCustomer) {
        CustomerLoginInfo cliOrg = customerLoginInfoRepository.findByCustomer(cloneCustomer);

        CustomerLoginInfo cliClone = new CustomerLoginInfo();
        cliClone.setUsername(cloneCustomer.getUser().getUsername());
        cliClone.setPassword(cliOrg.getPassword());
        cliClone.setToken(cliOrg.getToken());
        cliClone.setPasswordSet(cliOrg.isPasswordSet());
        cliClone.setCustomer(cloneCustomer);

        return cliClone;
    }

    private Customer cloneCustomer(Customer original) {
        Customer clone = new Customer();
        clone.setName("copy_" + original.getName());
        clone.setEmail("copy_" + original.getEmail());
        clone.setPosition(original.getPosition());
        clone.setPhone(original.getPhone());
        clone.setAddress(original.getAddress());
        clone.setCity(original.getCity());
        clone.setState(original.getState());
        clone.setCountry(original.getCountry());
        clone.setDescription(original.getDescription());
        clone.setTwitter(original.getTwitter());
        clone.setFacebook(original.getFacebook());
        clone.setYoutube(original.getYoutube());
        clone.setUser(original.getUser());
        clone.setCreatedAt(original.getCreatedAt());

        return clone;
    }
}
