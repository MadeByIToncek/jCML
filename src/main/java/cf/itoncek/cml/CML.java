package cf.itoncek.cml;

import com.novamaday.d4j.gradle.simplebot.GlobalCommandRegistrar;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.component.ActionComponent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.BanQuerySpec;
import discord4j.discordjson.json.MessageCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CML {

    public static Logger logger = LoggerFactory.getLogger("cf.itoncek.cml.CML");

    public static void main(final String[] args) {
        final String token = "NzA5NzA2NDE4NDQ1NjE1MTY0.Xrpzmw.krqW43oDjnPkFlHgBi16gJ0Yiew";
        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();

        try {
            new GlobalCommandRegistrar(client).registerCommands();
        } catch (Exception e) {
            logger.error(e.toString());
            logger.error(e.getMessage());
        }

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            final Message message = event.getMessage();
            if (message.getContent().startsWith("?purge")) {
                message.delete("Matyáš no...");
                message.getChannel().block().createMessage("Už se to prosím nauč Maty, žádný `?purge` tady není ;)");
            }
        });


        gateway.on(ChatInputInteractionEvent.class, event -> {
            switch (event.getCommandName()) {
                case "ping":
                    return event.reply("Beep boop, I'm awake").withEphemeral(true);
                case "jsem":
                    String id = event.getOption("tabor")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asString)
                            .orElse("null");
                    List<ActionComponent> actionComponentList = new ArrayList<>();
                    Button yes = Button.success("xyz" + event.getInteraction().getUser().getId().asLong() + "." + id, "Povolit");
                    Button no = Button.danger("rjct", "Odmítnout");
                    actionComponentList.add(yes);
                    actionComponentList.add(no);
                    ActionRow ac = ActionRow.of(actionComponentList);
                    MessageCreateRequest msg = MessageCreateRequest.builder().content(event.getInteraction().getUser().getMention() + " požádal o roli <@&" + id + ">").addComponent(ac.getData()).build();
                    client.getChannelById(Snowflake.of(931914067331923968L)).createMessage(msg).block();
                    return event.reply("Žádost o roli <@&" + id + "> byla odeslána").withEphemeral(true);
                case "ban":
                    User target = event.getOption("user")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asUser)
                            .orElse(null)
                            .block();
                    logger.info(target.getUsername());
                    event.getInteraction().getGuild().block().getMemberById(target.getId()).block().ban(BanQuerySpec.builder().reason("Banned by CML").build()).block();
                    return event.reply(target.getUsername() + " byl zabanován.").withEphemeral(true);
                case "kick":
                    User trg = event.getOption("user")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asUser)
                            .orElse(null)
                            .block();
                    event.getInteraction().getGuild().block().getMemberById(trg.getId()).block().kick("Banned by CML").block();
                    return event.reply(trg.getUsername() + " byl vyhozen.").withEphemeral(true);
                /*case "timeout":
                    if(Objects.requireNonNull(event.getInteraction().getGuild().block().getMemberById(event.getInteraction().getUser().getId()).block()).getBasePermissions().block().contains(Permission.BAN_MEMBERS)) {
                        User timeout = event.getOption("user")
                                .flatMap(ApplicationCommandInteractionOption::getValue)
                                .map(ApplicationCommandInteractionOptionValue::asUser)
                                .orElse(null)
                                .block();
                        Long minutes = event.getOption("time")
                                .flatMap(ApplicationCommandInteractionOption::getValue)
                                .map(ApplicationCommandInteractionOptionValue::asLong)
                                .orElse(null);
                        logger.info(timeout.getUsername() + timeout.getDiscriminator());
                        logger.info(String.valueOf(Duration.ofMinutes(minutes)));
                        client.getGuildById(event.getInteraction().getGuild().block().getId()).getMember(timeout.getId()).block().user().
                        MemberData to = client.getGuildById(event.getInteraction().getGuild().block().getId()).getMember(timeout.getId()).timeout(Duration.ofMinutes(minutes)).block();
                        return event.reply(timeout.getUsername() + " byl pozastaven.").withEphemeral(true);
                    } else {
                        Long minutes = event.getOption("time").get().getValue().get().asLong();
                        logger.info(String.valueOf(minutes));
                        gateway.getGuildById(event.getInteraction().getGuild().block().getId()).block().getMemberById(event.getInteraction().getUser().getId()).timeout(Duration.ofMinutes(minutes)).block();
                        return event.reply("⠐⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠂\n" +
                                "⠄⠄⣰⣾⣿⣿⣿⠿⠿⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣆⠄⠄\n" +
                                "⠄⠄⣿⣿⣿⡿⠋⠄⡀⣿⣿⣿⣿⣿⣿⣿⣿⠿⠛⠋⣉⣉⣉⡉⠙⠻⣿⣿⠄⠄\n" +
                                "⠄⠄⣿⣿⣿⣇⠔⠈⣿⣿⣿⣿⣿⡿⠛⢉⣤⣶⣾⣿⣿⣿⣿⣿⣿⣦⡀⠹⠄⠄\n" +
                                "⠄⠄⣿⣿⠃⠄⢠⣾⣿⣿⣿⠟⢁⣠⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡄⠄⠄\n" +
                                "⠄⠄⣿⣿⣿⣿⣿⣿⣿⠟⢁⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⠄⠄\n" +
                                "⠄⠄⣿⣿⣿⣿⣿⡟⠁⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠄⠄\n" +
                                "⠄⠄⣿⣿⣿⣿⠋⢠⣾⣿⣿⣿⣿⣿⣿⡿⠿⠿⠿⠿⣿⣿⣿⣿⣿⣿⣿⣿⠄⠄\n" +
                                "⠄⠄⣿⣿⡿⠁⣰⣿⣿⣿⣿⣿⣿⣿⣿⠗⠄⠄⠄⠄⣿⣿⣿⣿⣿⣿⣿⡟⠄⠄\n" +
                                "⠄⠄⣿⡿⠁⣼⣿⣿⣿⣿⣿⣿⡿⠋⠄⠄⠄⣠⣄⢰⣿⣿⣿⣿⣿⣿⣿⠃⠄⠄\n" +
                                "⠄⠄⡿⠁⣼⣿⣿⣿⣿⣿⣿⣿⡇⠄⢀⡴⠚⢿⣿⣿⣿⣿⣿⣿⣿⣿⡏⢠⠄⠄\n" +
                                "⠄⠄⠃⢰⣿⣿⣿⣿⣿⣿⡿⣿⣿⠴⠋⠄⠄⢸⣿⣿⣿⣿⣿⣿⣿⡟⢀⣾⠄⠄\n" +
                                "⠄⠄⢀⣿⣿⣿⣿⣿⣿⣿⠃⠈⠁⠄⠄⢀⣴⣿⣿⣿⣿⣿⣿⣿⡟⢀⣾⣿⠄⠄\n" +
                                "⠄⠄⢸⣿⣿⣿⣿⣿⣿⣿⠄⠄⠄⠄⢶⣿⣿⣿⣿⣿⣿⣿⣿⠏⢀⣾⣿⣿⠄⠄\n" +
                                "⠄⠄⣿⣿⣿⣿⣿⣿⣿⣷⣶⣶⣶⣶⣶⣿⣿⣿⣿⣿⣿⣿⠋⣠⣿⣿⣿⣿⠄⠄\n" +
                                "⠄⠄⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⢁⣼⣿⣿⣿⣿⣿⠄⠄\n" +
                                "⠄⠄⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⢁⣴⣿⣿⣿⣿⣿⣿⣿⠄⠄\n" +
                                "⠄⠄⠈⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠟⢁⣴⣿⣿⣿⣿⠗⠄⠄⣿⣿⠄⠄\n" +
                                "⠄⠄⣆⠈⠻⢿⣿⣿⣿⣿⣿⣿⠿⠛⣉⣤⣾⣿⣿⣿⣿⣿⣇⠠⠺⣷⣿⣿⠄⠄\n" +
                                "⠄⠄⣿⣿⣦⣄⣈⣉⣉⣉⣡⣤⣶⣿⣿⣿⣿⣿⣿⣿⣿⠉⠁⣀⣼⣿⣿⣿⠄⠄\n" +
                                "⠄⠄⠻⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣶⣶⣾⣿⣿⡿⠟⠄⠄\n" +
                                "⠠⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄").withEphemeral(true);
                    }*/
            }
            return null;
        }).subscribe();

        gateway.on(ButtonInteractionEvent.class, event -> {
            String id = event.getCustomId();
            if (id.startsWith("rjc")) {
                event.getMessage().get().delete("Denied");
                return event.reply("Uživatel byl odmítnut").withEphemeral(true);
            } else if (id.startsWith("xyz")) {
                id = id.substring(3);
                String userID = id.split("[.]")[0];
                String roleID = id.split("[.]")[1];
                Guild guild = event.getInteraction().getGuild().block();
                event.getMessage().get().delete("Allowed").block();
                guild.getMemberById(Snowflake.of(userID)).block().addRole(Snowflake.of(roleID)).block();
                return event.reply(gateway.getUserById(Snowflake.of(userID)).block().getUsername() + " byl povolen přístup k kanálům tábora '" + event.getInteraction().getGuild().block().getRoleById(Snowflake.of(roleID)).block().getName() + "'");
            }
            return event.reply("test").withEphemeral(true);
        }).subscribe();

        gateway.onDisconnect().block();
    }
}
