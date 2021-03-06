package my.sample.project;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.bittrade.libs.steemj.SteemJ;
import eu.bittrade.libs.steemj.base.models.AccountName;
import eu.bittrade.libs.steemj.base.models.AppliedOperation;
import eu.bittrade.libs.steemj.base.models.Permlink;
import eu.bittrade.libs.steemj.base.models.Vote;
import eu.bittrade.libs.steemj.base.models.VoteState;
import eu.bittrade.libs.steemj.base.models.operations.AccountCreateOperation;
import eu.bittrade.libs.steemj.base.models.operations.CommentOperation;
import eu.bittrade.libs.steemj.configuration.SteemJConfig;
import eu.bittrade.libs.steemj.enums.PrivateKeyType;
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException;
import eu.bittrade.libs.steemj.exceptions.SteemInvalidTransactionException;
import eu.bittrade.libs.steemj.exceptions.SteemResponseError;

/**
 * This class provides shows some common SteemJ commands.
 * 
 * @author <a href="http://steemit.com/@dez1337">dez1337</a>
 */
public class SteemJUsageExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteemJUsageExample.class);

    /**
     * Called at startup.
     * 
     * @param args
     *            The arguments to set.
     */
    public static void main(String args[]) {
        // Change the default settings if needed.
        SteemJConfig myConfig = SteemJConfig.getInstance();
        myConfig.setResponseTimeout(100000L);
        myConfig.setDefaultAccount(new AccountName("steemj"));

        try {
            // SteemJ already comes with a bunch of preconfigured EndPoints, but
            // if you want to connect to another one, you can simply configure
            // it as shown here:

            // Reset the currently configured endpoints.
            myConfig.setWebSocketEndpointURIs(new ArrayList<Pair<URI, Boolean>>());
            // Change the default settings if needed.
            myConfig.addWebSocketEndpointURI(new URI("wss://seed.bitcoiner.me"), true);
        } catch (URISyntaxException e) {
            throw new RuntimeException("The given URI is not valid.", e);
        }

        try {
            // Create a new apiWrapper with your config object.
            SteemJ steemJ = new SteemJ();

            List<ImmutablePair<PrivateKeyType, String>> privateKeys = new ArrayList<>();
            privateKeys.add(new ImmutablePair<>(PrivateKeyType.POSTING, "YOUR-PRIVATE-POSTING-KEY"));
            privateKeys.add(new ImmutablePair<>(PrivateKeyType.ACTIVE, "YOUR-PRIVATE-ACTIVE-KEY"));

            myConfig.getPrivateKeyStorage().addAccount(myConfig.getDefaultAccount(), privateKeys);

            // Let's have a look at the account history of dez1337
            Map<Integer, AppliedOperation> accountHistory = steemJ.getAccountHistory(new AccountName("dez1337"), 100,
                    100);
            if (accountHistory.get(0).getOp() instanceof AccountCreateOperation) {
                AccountCreateOperation accountCreateOperation = (AccountCreateOperation) (accountHistory.get(0)
                        .getOp());
                LOGGER.info("The account {} has been created by {}.", "dez1337", accountCreateOperation.getCreator());
            }

            /*
             * Upvote the post
             * "steem-java-api-learned-to-speak-graphene-update-5" written by
             * "dez1337" using 100% of the defaultAccounts voting power.
             */
            steemJ.vote(new AccountName("dez1337"), new Permlink("steem-java-api-learned-to-speak-graphene-update-5"),
                    (short) 100);

            /*
             * Remove the vote made earlier.
             */
            steemJ.cancelVote(new AccountName("dez1337"),
                    new Permlink("steem-java-api-learned-to-speak-graphene-update-5"));

            // Let the default account ("steemj") follow "cyriana"
            steemJ.follow(new AccountName("cyriana"));

            // Let the default account ("steemj") unfollow "cyriana"
            steemJ.unfollow(new AccountName("cyriana"));

            // Let the default account ("steemj") resteem a post".
            steemJ.reblog(new AccountName("dez1337"),
                    new Permlink("steemj-v0-4-0-has-been-released-integrate-steem-into-your-java-project"));

            /*
             * Write a new post.
             * 
             * Title = "Test of SteemJ 0.4.0" Content =
             * "Test using SteemJ 0. ..... " Tags = "test", "dontvote"
             * 
             */
            CommentOperation myNewPost = steemJ.createPost("Test of SteemJ 0.4.0",
                    "Test using SteemJ 0.4.0 by @dez1337 with a link to "
                            + "https://github.com/marvin-we/steem-java-api-wrapper "
                            + "and an image ![SteemJV2Logo](https://imgur.com/bIhZlYT.png).",
                    new String[] { "test", "dontvote" });
            LOGGER.info(
                    "SteemJ has generated some additional values for my new post. One good example is the permlink {} that I may need later on.",
                    myNewPost.getPermlink().getLink());

            /*
             * Write a new comment.
             * 
             * Author of the post to reply to = "steemj" Permlink of the post to
             * reply to = "testofsteemj040" Title = "Test of SteemJ 0.4.0"
             * Content = "Test using SteemJ 0. ..... " Tags = "test"
             * 
             */
            steemJ.createComment(new AccountName("steemj"), new Permlink("testofsteemj040"),
                    "Example comment without a link but with a @user .", new String[] { "test" });

            /*
             * Delete the newly created post.
             */
            steemJ.deletePostOrComment(myNewPost.getParentPermlink());

            // Get the current Price
            LOGGER.info("The current price in the internal market is {}.",
                    steemJ.getCurrentMedianHistoryPrice().getBase().getAmount());

            // Get votes
            List<Vote> votes = steemJ.getAccountVotes(new AccountName("dez1337"));
            LOGGER.info("The user dez1337 has done {} votes so far.", votes.size());
            LOGGER.info("His last vote has been done on {}.", votes.get(votes.size() - 1).getTime());
            int numberOfAccounts = steemJ.getAccountCount();
            int numberOfWitnesses = steemJ.getWitnessCount();
            LOGGER.info(
                    "dez1337 is one of {} accounts on steemit and may increase the number witnesses to {} in the near future.",
                    numberOfAccounts, numberOfWitnesses + 1);

            List<VoteState> activeVotesForArticle = steemJ.getActiveVotes(new AccountName("dez1337"),
                    new Permlink("steem-api-wrapper-for-java-update1"));
            LOGGER.info("The last vote for a article of dez1337 has been done from {}.",
                    activeVotesForArticle.get(activeVotesForArticle.size() - 1).getVoter());
            LOGGER.info(
                    "You may also want to vote for some posts to generate some Steem which is currently worth about {}.",
                    steemJ.getCurrentMedianHistoryPrice().getBase());

            // Force an error response:
            steemJ.getAccountVotes(new AccountName("thisAcountDoesNotExistYet"));
        } catch (SteemResponseError e) {
            // The SteemResponseError contains the error response.
            LOGGER.error("An error with code {} occured with the following message {}.",
                    e.getError().getSteemErrorDetails().getData().getCode(),
                    e.getError().getSteemErrorDetails().getMessage());
        } catch (SteemCommunicationException e) {
            LOGGER.error("A communication error occured!", e);
        } catch (SteemInvalidTransactionException e) {
            LOGGER.error("There was a problem to sign a transaction.", e);
        }
    }

}