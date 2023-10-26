

@repository
public interface HelpRequestRepository extends CrudRepository<HelpRequest, Long> {

    Iterable<HelpRequest> findAllByRequesterEmail(String requesterEmail);
    
}
