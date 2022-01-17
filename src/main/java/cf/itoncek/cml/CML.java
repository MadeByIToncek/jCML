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
            switch(event.getCommandName()){
                case "ping":
                    return event.reply("Beep boop, I'm awake").withEphemeral(true);
                case "jsem":
                    String id = event.getOption("tabor")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asString)
                            .orElse("null");
                    List<ActionComponent> actionComponentList = new ArrayList<>();
                    Button yes = Button.success("xyz"+event.getInteraction().getUser().getId().asLong() + "." + id , "Povolit");
                    Button no = Button.danger("rjct" , "Odmítnout");
                    actionComponentList.add(yes);
                    actionComponentList.add(no);
                    ActionRow ac = ActionRow.of(actionComponentList);
                    MessageCreateRequest msg = MessageCreateRequest.builder().content(event.getInteraction().getUser().getMention() + " požádal o roli <@&" + id + ">").addComponent(ac.getData()).build();
                    client.getChannelById(Snowflake.of(931914067331923968L)).createMessage(msg).block();
                    return event.reply("Žádost o roli <@&"+id+"> byla odeslána").withEphemeral(true);
            }
            return null;
        }).subscribe();

        gateway.on(ButtonInteractionEvent.class, event -> {
            String id = event.getCustomId();
            if (id.startsWith("rjc")){
                event.getMessage().get().delete("Denied");
                return event.reply("Uživatel byl odmítnut").withEphemeral(true);
            } else if (id.startsWith("xyz")){
                id = id.substring(3);
                String userID = id.split("[.]")[0];
                String roleID = id.split("[.]")[1];
                Guild guild = event.getInteraction().getGuild().block();
                event.getMessage().get().delete("Allowed").block();
                guild.getMemberById(Snowflake.of(userID)).block().addRole(Snowflake.of(roleID)).block();
                return event.reply(gateway.getUserById(Snowflake.of(userID)).block().getUsername() + " byl povolen přístup k kanálům tábora '"+ event.getInteraction().getGuild().block().getRoleById(Snowflake.of(roleID)).block().getName()+"'");
            }
            return event.reply("test").withEphemeral(true);
        }).subscribe();

        gateway.onDisconnect().block();
    }
}
