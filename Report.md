Feed Reader
==============

## Assumptions
- Articles can be arbitrarily large. 
  
## Design decisions
- Metadata information is persisted in a local database. This can be replaced by a RDBMS system 
while operationalizing this application.
- All the REST API calls are read/write protected and appropriately synchronized to ensure 
consistent view of the system state.
- Database referential integrity guarantees that system will not be stuck in deadlocked or 
unrecoverable state.

## Available APIs
- User management API
   - Add user
     * URL: /api/1/users
     * Method : POST
     * Argument: name.
     * Eg., /api/1/users?name=karthik
   - Delete user
        * URL: /api/1/users
        * Method : DELETE
        * Argument: name/id
        * Eg., /api/1/users?name=karthik
- Subscription API  
   - Subscribe
        * URL: /api/1/subscriptions
        * Method : POST
        * Argument: name/id, feed
        * Eg., /api/1/subscriptions?name=karthik&feed=tech
   - Unsubscribe
           * URL: /api/1/subscriptions
           * Method : DELETE
           * Argument: name/id, feed
           * Eg., /api/1/subscriptions?name=karthik&feed=tech
- Article management                
   - Add article
           * URL: /api/1/articles
           * Method : POST
           * Argument: feed
           * Content-type: application/json
           * body: {title:<title of the article>, body: <body of the article>}
           * Eg., /api/1/articles?feed=tech
   - Get article
           * URL: /api/1/articles
           * Method : GET
           * Argument: name/id
           * Eg., /api/1/articles?name=karthik             
- Feed Management
   - Add a feed
           * URL: /api/1/feeds
           * Method : POST
           * Argument: feed
           * Eg., /api/1/feeds?feed=tech
   - Get all subscribed feeds for an user
           * URL: /api/1/feeds
           * Method : GET 
           * Argument: name/id
           * Eg., /api/1/feeds?name=karthik&
 