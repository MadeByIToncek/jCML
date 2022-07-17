package cf.itoncek.cml;

import com.udojava.evalex.Expression;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.component.ActionComponent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.spec.BanQuerySpec;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.rest.util.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CML {
    
    public static final Logger logger = LoggerFactory.getLogger("cf.itoncek.cml.CML");
    
    public static void main(final String[] args) {
        if(args.length > 0) {
            if(args[0].equals("-v") || args[0].equals("--version")) {
                logger.info("CML 1.0.1");
            } else if(args[0].equals("-h") || args[0].equals("--healthcheck")) {
                long ping = pingHost("https://discordapp.com/api/v6/gateway", 443, 5000);
                if(ping > 0) {
                    logger.info("CML is able to connect to Discord. Current delay: " + ping + "ms");
                } else {
                    logger.info("CML is unable to contact the Discord gateway!");
                }
            }
        } else {
            // ENVs required:
            // - TOKEN
            // - V_CHANNEL
            // - GUILD_ID
            final String token = System.getenv("TOKEN");
            final DiscordClient client = DiscordClient.create(token);
            final GatewayDiscordClient gateway = client.login().block();
            
            try {
                new GlobalCommandRegistrar(client).registerCommands();
            } catch (Exception e) {
                logger.error(e.getMessage());
                for (StackTraceElement element : e.getStackTrace()) {
                    logger.error(element.toString());
                }
                logger.error(e.getLocalizedMessage());
            }
            
            assert gateway != null;
            gateway.on(ChatInputInteractionEvent.class, e -> {
                switch (e.getCommandName()) {
                    case "ping":
                        return e.reply("Beep boop, I'm awake").withEphemeral(true);
                    case "jsem":
                        String id = e.getOption("tabor")
                                .flatMap(ApplicationCommandInteractionOption::getValue)
                                .map(ApplicationCommandInteractionOptionValue::asString)
                                .orElse("null");
                        List<ActionComponent> actionComponentList = new ArrayList<>();
                        Button yes = Button.success("xyz" + e.getInteraction().getUser().getId().asLong() + "." + id, "Povolit");
                        Button no = Button.danger("rjct", "Odmítnout");
                        actionComponentList.add(yes);
                        actionComponentList.add(no);
                        ActionRow ac = ActionRow.of(actionComponentList);
                        MessageCreateRequest msg = MessageCreateRequest.builder().content(e.getInteraction().getUser().getMention() + " požádal o roli <@&" + id + ">").addComponent(ac.getData()).build();
                        client.getChannelById(Snowflake.of(Long.parseLong(System.getenv("V_CHANNEL")))).createMessage(msg).block();
                        return e.reply("Žádost o roli <@&" + id + "> byla odeslána").withEphemeral(true);
                    case "ban":
                        if(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(e.getInteraction().getGuild().block()).getMemberById(e.getInteraction().getUser().getId()).block()).getBasePermissions().block()).contains(Permission.BAN_MEMBERS)) {
                            User target = Objects.requireNonNull(e.getOption("user")
                                                                         .flatMap(ApplicationCommandInteractionOption::getValue)
                                                                         .map(ApplicationCommandInteractionOptionValue::asUser)
                                                                         .orElse(null))
                                    .block();
                            assert target != null;
                            logger.info(target.getUsername());
                            Objects.requireNonNull(Objects.requireNonNull(e.getInteraction().getGuild().block()).getMemberById(target.getId()).block()).ban(BanQuerySpec.builder().reason("Banned by CML").build()).block();
                            return e.reply(target.getUsername() + " byl zabanován.").withEphemeral(true);
                        } else {
                            return e.reply("""
                                                   ⠐⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠂
                                                   ⠄⠄⣰⣾⣿⣿⣿⠿⠿⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣆⠄⠄
                                                   ⠄⠄⣿⣿⣿⡿⠋⠄⡀⣿⣿⣿⣿⣿⣿⣿⣿⠿⠛⠋⣉⣉⣉⡉⠙⠻⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣿⣇⠔⠈⣿⣿⣿⣿⣿⡿⠛⢉⣤⣶⣾⣿⣿⣿⣿⣿⣿⣦⡀⠹⠄⠄
                                                   ⠄⠄⣿⣿⠃⠄⢠⣾⣿⣿⣿⠟⢁⣠⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡄⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⣿⣿⣿⠟⢁⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⣿⡟⠁⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⠋⢠⣾⣿⣿⣿⣿⣿⣿⡿⠿⠿⠿⠿⣿⣿⣿⣿⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⡿⠁⣰⣿⣿⣿⣿⣿⣿⣿⣿⠗⠄⠄⠄⠄⣿⣿⣿⣿⣿⣿⣿⡟⠄⠄
                                                   ⠄⠄⣿⡿⠁⣼⣿⣿⣿⣿⣿⣿⡿⠋⠄⠄⠄⣠⣄⢰⣿⣿⣿⣿⣿⣿⣿⠃⠄⠄
                                                   ⠄⠄⡿⠁⣼⣿⣿⣿⣿⣿⣿⣿⡇⠄⢀⡴⠚⢿⣿⣿⣿⣿⣿⣿⣿⣿⡏⢠⠄⠄
                                                   ⠄⠄⠃⢰⣿⣿⣿⣿⣿⣿⡿⣿⣿⠴⠋⠄⠄⢸⣿⣿⣿⣿⣿⣿⣿⡟⢀⣾⠄⠄
                                                   ⠄⠄⢀⣿⣿⣿⣿⣿⣿⣿⠃⠈⠁⠄⠄⢀⣴⣿⣿⣿⣿⣿⣿⣿⡟⢀⣾⣿⠄⠄
                                                   ⠄⠄⢸⣿⣿⣿⣿⣿⣿⣿⠄⠄⠄⠄⢶⣿⣿⣿⣿⣿⣿⣿⣿⠏⢀⣾⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⣿⣿⣿⣷⣶⣶⣶⣶⣶⣿⣿⣿⣿⣿⣿⣿⠋⣠⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⢁⣼⣿⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⢁⣴⣿⣿⣿⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⠈⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠟⢁⣴⣿⣿⣿⣿⠗⠄⠄⣿⣿⠄⠄
                                                   ⠄⠄⣆⠈⠻⢿⣿⣿⣿⣿⣿⣿⠿⠛⣉⣤⣾⣿⣿⣿⣿⣿⣇⠠⠺⣷⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣦⣄⣈⣉⣉⣉⣡⣤⣶⣿⣿⣿⣿⣿⣿⣿⣿⠉⠁⣀⣼⣿⣿⣿⠄⠄
                                                   ⠄⠄⠻⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣶⣶⣾⣿⣿⡿⠟⠄⠄
                                                   ⠠⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄""");
                            
                        }
                    case "kick":
                        if(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(e.getInteraction().getGuild().block()).getMemberById(e.getInteraction().getUser().getId()).block()).getBasePermissions().block()).contains(Permission.KICK_MEMBERS)) {
                            User trg = Objects.requireNonNull(e.getOption("user")
                                                                      .flatMap(ApplicationCommandInteractionOption::getValue)
                                                                      .map(ApplicationCommandInteractionOptionValue::asUser)
                                                                      .orElse(null))
                                    .block();
                            assert trg != null;
                            Objects.requireNonNull(Objects.requireNonNull(e.getInteraction().getGuild().block()).getMemberById(trg.getId()).block()).kick("Banned by CML").block();
                            return e.reply(trg.getUsername() + " byl vyhozen.").withEphemeral(true);
                        } else {
                            return e.reply("""
                                                   ⠐⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠂
                                                   ⠄⠄⣰⣾⣿⣿⣿⠿⠿⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣆⠄⠄
                                                   ⠄⠄⣿⣿⣿⡿⠋⠄⡀⣿⣿⣿⣿⣿⣿⣿⣿⠿⠛⠋⣉⣉⣉⡉⠙⠻⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣿⣇⠔⠈⣿⣿⣿⣿⣿⡿⠛⢉⣤⣶⣾⣿⣿⣿⣿⣿⣿⣦⡀⠹⠄⠄
                                                   ⠄⠄⣿⣿⠃⠄⢠⣾⣿⣿⣿⠟⢁⣠⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡄⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⣿⣿⣿⠟⢁⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⣿⡟⠁⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⠋⢠⣾⣿⣿⣿⣿⣿⣿⡿⠿⠿⠿⠿⣿⣿⣿⣿⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⡿⠁⣰⣿⣿⣿⣿⣿⣿⣿⣿⠗⠄⠄⠄⠄⣿⣿⣿⣿⣿⣿⣿⡟⠄⠄
                                                   ⠄⠄⣿⡿⠁⣼⣿⣿⣿⣿⣿⣿⡿⠋⠄⠄⠄⣠⣄⢰⣿⣿⣿⣿⣿⣿⣿⠃⠄⠄
                                                   ⠄⠄⡿⠁⣼⣿⣿⣿⣿⣿⣿⣿⡇⠄⢀⡴⠚⢿⣿⣿⣿⣿⣿⣿⣿⣿⡏⢠⠄⠄
                                                   ⠄⠄⠃⢰⣿⣿⣿⣿⣿⣿⡿⣿⣿⠴⠋⠄⠄⢸⣿⣿⣿⣿⣿⣿⣿⡟⢀⣾⠄⠄
                                                   ⠄⠄⢀⣿⣿⣿⣿⣿⣿⣿⠃⠈⠁⠄⠄⢀⣴⣿⣿⣿⣿⣿⣿⣿⡟⢀⣾⣿⠄⠄
                                                   ⠄⠄⢸⣿⣿⣿⣿⣿⣿⣿⠄⠄⠄⠄⢶⣿⣿⣿⣿⣿⣿⣿⣿⠏⢀⣾⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⣿⣿⣿⣷⣶⣶⣶⣶⣶⣿⣿⣿⣿⣿⣿⣿⠋⣠⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⢁⣼⣿⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⢁⣴⣿⣿⣿⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⠈⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠟⢁⣴⣿⣿⣿⣿⠗⠄⠄⣿⣿⠄⠄
                                                   ⠄⠄⣆⠈⠻⢿⣿⣿⣿⣿⣿⣿⠿⠛⣉⣤⣾⣿⣿⣿⣿⣿⣇⠠⠺⣷⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣦⣄⣈⣉⣉⣉⣡⣤⣶⣿⣿⣿⣿⣿⣿⣿⣿⠉⠁⣀⣼⣿⣿⣿⠄⠄
                                                   ⠄⠄⠻⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣶⣶⣾⣿⣿⡿⠟⠄⠄
                                                   ⠠⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄""").withEphemeral(true);
                        }
                    case "rolepurge":
                        
                        if(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(e.getInteraction().getGuild().block()).getMemberById(e.getInteraction().getUser().getId()).block()).getBasePermissions().block()).contains(Permission.MANAGE_ROLES)) {
                            methodThatTakesALongTime(e);
                            return e.deferReply().withEphemeral(true);
                            
                        } else {
                            return e.reply("""
                                                   ⠐⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠂
                                                   ⠄⠄⣰⣾⣿⣿⣿⠿⠿⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣆⠄⠄
                                                   ⠄⠄⣿⣿⣿⡿⠋⠄⡀⣿⣿⣿⣿⣿⣿⣿⣿⠿⠛⠋⣉⣉⣉⡉⠙⠻⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣿⣇⠔⠈⣿⣿⣿⣿⣿⡿⠛⢉⣤⣶⣾⣿⣿⣿⣿⣿⣿⣦⡀⠹⠄⠄
                                                   ⠄⠄⣿⣿⠃⠄⢠⣾⣿⣿⣿⠟⢁⣠⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡄⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⣿⣿⣿⠟⢁⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⣿⡟⠁⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⠋⢠⣾⣿⣿⣿⣿⣿⣿⡿⠿⠿⠿⠿⣿⣿⣿⣿⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⡿⠁⣰⣿⣿⣿⣿⣿⣿⣿⣿⠗⠄⠄⠄⠄⣿⣿⣿⣿⣿⣿⣿⡟⠄⠄
                                                   ⠄⠄⣿⡿⠁⣼⣿⣿⣿⣿⣿⣿⡿⠋⠄⠄⠄⣠⣄⢰⣿⣿⣿⣿⣿⣿⣿⠃⠄⠄
                                                   ⠄⠄⡿⠁⣼⣿⣿⣿⣿⣿⣿⣿⡇⠄⢀⡴⠚⢿⣿⣿⣿⣿⣿⣿⣿⣿⡏⢠⠄⠄
                                                   ⠄⠄⠃⢰⣿⣿⣿⣿⣿⣿⡿⣿⣿⠴⠋⠄⠄⢸⣿⣿⣿⣿⣿⣿⣿⡟⢀⣾⠄⠄
                                                   ⠄⠄⢀⣿⣿⣿⣿⣿⣿⣿⠃⠈⠁⠄⠄⢀⣴⣿⣿⣿⣿⣿⣿⣿⡟⢀⣾⣿⠄⠄
                                                   ⠄⠄⢸⣿⣿⣿⣿⣿⣿⣿⠄⠄⠄⠄⢶⣿⣿⣿⣿⣿⣿⣿⣿⠏⢀⣾⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⣿⣿⣿⣷⣶⣶⣶⣶⣶⣿⣿⣿⣿⣿⣿⣿⠋⣠⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⢁⣼⣿⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⢁⣴⣿⣿⣿⣿⣿⣿⣿⠄⠄
                                                   ⠄⠄⠈⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠟⢁⣴⣿⣿⣿⣿⠗⠄⠄⣿⣿⠄⠄
                                                   ⠄⠄⣆⠈⠻⢿⣿⣿⣿⣿⣿⣿⠿⠛⣉⣤⣾⣿⣿⣿⣿⣿⣇⠠⠺⣷⣿⣿⠄⠄
                                                   ⠄⠄⣿⣿⣦⣄⣈⣉⣉⣉⣡⣤⣶⣿⣿⣿⣿⣿⣿⣿⣿⠉⠁⣀⣼⣿⣿⣿⠄⠄
                                                   ⠄⠄⠻⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣶⣶⣾⣿⣿⡿⠟⠄⠄
                                                   ⠠⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄""").withEphemeral(true);
                        }
                    case "express":
                        String result;
                        String exp;
                        if(e.getOption("expression").flatMap(ApplicationCommandInteractionOption::getValue).isPresent()) {
                            exp = e.getOption("expression").flatMap(ApplicationCommandInteractionOption::getValue).get().asString();
                            try {
                                Expression expression = new Expression(exp);
                                expression.setPrecision(100);
                                result = expression.eval().toString();
                            } catch (Exception ex) {
                                result = "Error: " + ex.getMessage();
                            }
                        } else {
                            result = "No expression provided";
                            exp = null;
                        }
                        return e.reply().withContent("```" + exp + "``` :arrow_double_down: ```" + result + "```");
                }
                return e.reply("c");
            }).subscribe();
            
            gateway.on(ButtonInteractionEvent.class, event -> {
                String id = event.getCustomId();
                if(id.startsWith("rjc")) {
                    if(event.getMessage().isPresent()) {
                        event.getMessage().get().delete("Denied");
                    }
                    return event.reply("Uživatel byl odmítnut").withEphemeral(true);
                } else if(id.startsWith("xyz")) {
                    id = id.substring(3);
                    String userID = id.split("[.]")[0];
                    String roleID = id.split("[.]")[1];
                    Guild guild = event.getInteraction().getGuild().block();
                    event.getMessage().get().delete("Allowed").block();
                    assert guild != null;
                    Objects.requireNonNull(guild.getMemberById(Snowflake.of(userID)).block()).addRole(Snowflake.of(roleID)).block();
                    return event.reply(Objects.requireNonNull(gateway.getUserById(Snowflake.of(userID)).block()).getUsername() + " byl povolen přístup k kanálům tábora '" + Objects.requireNonNull(Objects.requireNonNull(event.getInteraction().getGuild().block()).getRoleById(Snowflake.of(roleID)).block()).getName() + "'");
                }
                return event.reply("test").withEphemeral(true);
            }).subscribe();
            
            gateway.onDisconnect().block();
        }
    }
    
    private static void methodThatTakesALongTime(ChatInputInteractionEvent event) {
        User target = Objects.requireNonNull(event.getOption("user")
                                                     .flatMap(ApplicationCommandInteractionOption::getValue)
                                                     .map(ApplicationCommandInteractionOptionValue::asUser)
                                                     .orElse(null))
                .block();
        assert target != null;
        Objects.requireNonNull(Objects.requireNonNull(event.getInteraction().getGuild().block()).getMemberById(target.getId()).block()).getRoleIds().forEach(snowflake -> Objects.requireNonNull(Objects.requireNonNull(event.getInteraction().getGuild().block()).getMemberById(target.getId()).block()).removeRole(snowflake).block());
        event.createFollowup("This took awhile but I'm done!");
    }
    
    public static long pingHost(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            Instant start = Instant.now();
            socket.connect(new InetSocketAddress(host, port), timeout);
            Instant end = Instant.now();
            return end.getLong(ChronoField.MILLI_OF_DAY) - start.getLong(ChronoField.MILLI_OF_DAY);
        } catch (IOException e) {
            return -1; // Either timeout or unreachable or failed DNS lookup.
        }
    }
}
