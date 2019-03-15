/*
 * Copyright (c)
 * Created and developed by Antonio112009
 */

package webServer;

import Entity.GetDay;
import Entity.Subject;
import actions.Cutter;
import actions.General;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.List;

public class Webpage {

    public void generateWebpage(String telegramId, GetDay getDay){
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML");
        templateEngine.setTemplateResolver(templateResolver);
        Context context = new Context();

        List<Subject[]> subjects = new Cutter().showTimetable(telegramId, getDay);
        context.setVariable("subjects", subjects);
        StringWriter stringWriter = new StringWriter();
        templateEngine.process("test.html", context, stringWriter);

        try {
            File tempFile = new File("calendars/webCalendars/calendars-" + new General().getDayString(getDay) + "/web-" + telegramId + ".html");
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            writer.write(stringWriter.toString());
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}