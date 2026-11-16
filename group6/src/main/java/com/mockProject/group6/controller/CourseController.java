
package com.mockProject.group6.controller;

import com.mockProject.group6.service.CourseService;
import com.mockProject.group6.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    private User signedIn(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    @GetMapping("/courses")
    public String catalog(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer category,
            Model model,
            HttpSession session) {

        model.addAttribute("courses", service.catalog(q, category));
        model.addAttribute("categories", service.categories());
        model.addAttribute("keyword", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("currentUser", signedIn(session));

        return "course/catalog";
    }

    @GetMapping("/courses/{id}")
    public String details(
            @PathVariable int id,
            Model model,
            HttpSession session) {

        Map<String, Object> course = service.course(id);
        User user = signedIn(session);

        boolean enrolled = user != null
                && service.isEnrolled(id, user.getId());

        boolean owner = user != null
                && Objects.equals(
                        ((Number) course.get("instructorId")).intValue(),
                        user.getId()
                );

        if (!"PUBLISHED".equals(String.valueOf(course.get("status"))) && !owner) {
            return "redirect:/courses";
        }

        model.addAttribute("course", course);
        model.addAttribute("enrolled", enrolled);
        model.addAttribute("owner", owner);
        model.addAttribute("currentUser", user);

        return "course/details";
    }

    @PostMapping("/courses/{id}/enroll")
    public String enroll(
            @PathVariable int id,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            service.enroll(id, user.getId());
            flash.addFlashAttribute("msg", "You have successfully joined the course.");
            return "redirect:/my-learning";
        } catch (RuntimeException e) {
            flash.addFlashAttribute("err", e.getMessage());
            return "redirect:/courses/" + id;
        }
    }

    @GetMapping("/my-learning")
    public String myLearning(Model model, HttpSession session) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> courses = service.myEnrollments(user.getId());

        for (Map<String, Object> course : courses) {
            int total = ((Number) course.get("totalLessons")).intValue();
            int done = ((Number) course.get("completedLessons")).intValue();

            course.put("percent",
                    total == 0 ? 0 : (int) Math.round(done * 100.0 / total));
        }

        model.addAttribute("courses", courses);
        model.addAttribute("currentUser", user);

        return "course/my-learning";
    }

    @GetMapping("/my-courses")
    public String myCourses(Model model, HttpSession session) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> courses = service.myCreatedCourses(user.getId());

        for (Map<String, Object> course : courses) {
            int total = ((Number) course.get("lessonCount")).intValue();
            int done = ((Number) course.get("completedLessons")).intValue();

            course.put("percent",
                    total == 0 ? 0 : (int) Math.round(done * 100.0 / total));
        }

        model.addAttribute("courses", courses);
        model.addAttribute("currentUser", user);

        return "course/my-courses";
    }

    @GetMapping("/my-courses/new")
    public String newCourse(Model model, HttpSession session) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("categories", service.categories());
        model.addAttribute("currentUser", user);

        return "course/course-form";
    }

    @PostMapping("/my-courses")
    public String createCourse(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam int categoryId,
            @RequestParam(defaultValue = "Beginner") String level,
            @RequestParam(defaultValue = "0") BigDecimal price,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String lessons,
            HttpSession session,
            RedirectAttributes flash,
            Model model) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            int id = service.createPersonalCourse(
                    user, title, description, categoryId,
                    level, price, imageUrl, lessons
            );

            flash.addFlashAttribute(
                    "msg",
                    "Your private course has been created."
            );

            return "redirect:/my-courses/" + id + "/edit";

        } catch (RuntimeException e) {
            model.addAttribute("err", e.getMessage());
            model.addAttribute("categories", service.categories());
            model.addAttribute("currentUser", user);

            return "course/course-form";
        }
    }

    @GetMapping("/my-courses/{id}/edit")
    public String editPersonalCourse(
            @PathVariable int id,
            Model model,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        Map<String, Object> course = service.course(id);

        if (!service.isPersonalOwner(id, user.getId())) {
            flash.addFlashAttribute(
                    "err",
                    "You can only manage your own private courses."
            );
            return "redirect:/my-courses";
        }

        model.addAttribute("course", course);
        model.addAttribute("currentUser", user);
        model.addAttribute("outline", service.outline(id, user.getId()));
        model.addAttribute("sectionsFlat", service.sectionsFlat(id));

        return "course/personal-course";
    }

    @PostMapping("/my-courses/{id}/edit")
    public String updatePersonalCourse(
            @PathVariable int id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            service.updatePersonalCourse(
                    id, user.getId(), title, description
            );
            flash.addFlashAttribute("msg", "Your private course has been updated.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("err", e.getMessage());
        }

        return "redirect:/my-courses/" + id + "/edit";
    }

    @PostMapping("/my-courses/{id}/delete")
    public String deletePersonalCourse(
            @PathVariable int id,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            service.deletePersonalCourse(id, user.getId());
            flash.addFlashAttribute("msg", "Your private course has been deleted.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("err", e.getMessage());
        }

        return "redirect:/my-courses";
    }

    @PostMapping("/my-courses/{id}/lessons")
    public String addPersonalLesson(
            @PathVariable int id,
            @RequestParam String title,
            @RequestParam(required = false) String content,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            service.addPersonalLesson(id, user.getId(), title, content);
            flash.addFlashAttribute("msg", "Lesson added to your private course.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("err", e.getMessage());
        }

        return "redirect:/my-courses/" + id + "/edit";
    }

    @GetMapping("/courses/{id}/learn")
    public String learn(
            @PathVariable int id,
            Model model,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        if (!service.isEnrolled(id, user.getId())
                && !service.isPersonalOwner(id, user.getId())) {

            flash.addFlashAttribute(
                    "err",
                    "Please enroll before starting this course."
            );

            return "redirect:/courses/" + id;
        }

        model.addAttribute("course", service.course(id));
        model.addAttribute("lessons", service.lessons(id));
        model.addAttribute("completed",
                service.completedLessonIds(id, user.getId()));
        model.addAttribute("outline", service.outline(id, user.getId()));
        model.addAttribute("currentUser", user);

        return "course/learning";
    }

    @GetMapping("/courses/{courseId}/lessons/{lessonId}")
    public String lessonPage(
            @PathVariable int courseId,
            @PathVariable int lessonId,
            Model model,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        if (!service.isEnrolled(courseId, user.getId())
                && !service.isPersonalOwner(courseId, user.getId())) {

            flash.addFlashAttribute("err", "Enroll before opening lessons.");
            return "redirect:/courses/" + courseId;
        }

        Map<String, Object> lesson = service.lesson(courseId, lessonId);
        List<Map<String, Object>> all = service.lessons(courseId);

        int index = -1;

        for (int i = 0; i < all.size(); i++) {
            if (((Number) all.get(i).get("lessonId")).intValue() == lessonId) {
                index = i;
                break;
            }
        }

        model.addAttribute("course", service.course(courseId));
        model.addAttribute("lesson", lesson);
        model.addAttribute("previous", index > 0 ? all.get(index - 1) : null);
        model.addAttribute("next",
                index >= 0 && index + 1 < all.size()
                        ? all.get(index + 1)
                        : null);
        model.addAttribute("done",
                service.completedLessonIds(courseId, user.getId())
                        .contains(lessonId));
        model.addAttribute("currentUser", user);

        return "course/lesson";
    }

    @PostMapping("/courses/{courseId}/lessons/{lessonId}/progress")
    public String progress(
            @PathVariable int courseId,
            @PathVariable int lessonId,
            @RequestParam(defaultValue = "true") boolean done,
            @RequestParam(defaultValue = "false") boolean returnToLesson,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            service.markLesson(courseId, lessonId, user.getId(), done);

            flash.addFlashAttribute(
                    "msg",
                    done ? "Lesson marked as completed."
                            : "Lesson marked as incomplete."
            );

        } catch (RuntimeException e) {
            flash.addFlashAttribute("err", e.getMessage());
        }

        if (returnToLesson) {
            return "redirect:/courses/" + courseId + "/lessons/" + lessonId;
        }

        return "redirect:/courses/" + courseId + "/learn";
    }

    @GetMapping("/my-courses/{courseId}/lessons/{lessonId}/edit")
    public String lessonEdit(
            @PathVariable int courseId,
            @PathVariable int lessonId,
            Model model,
            HttpSession session) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        if (!service.isPersonalOwner(courseId, user.getId())) {
            return "redirect:/my-courses";
        }

        model.addAttribute("course", service.course(courseId));
        model.addAttribute("lesson", service.lesson(courseId, lessonId));
        model.addAttribute("sectionsFlat", service.sectionsFlat(courseId));
        model.addAttribute("currentUser", user);

        return "course/lesson-edit";
    }

    @PostMapping("/my-courses/{courseId}/lessons/{lessonId}/edit")
    public String lessonSave(
            @PathVariable int courseId,
            @PathVariable int lessonId,
            @RequestParam int sectionId,
            @RequestParam String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String contentUrl,
            @RequestParam(defaultValue = "TEXT") String type,
            @RequestParam(defaultValue = "5") int minutes,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            service.updateLesson(
                    courseId, user.getId(), lessonId,
                    sectionId, title, content, contentUrl, type, minutes
            );
            flash.addFlashAttribute("msg", "Lesson saved.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("err", e.getMessage());
        }

        return "redirect:/my-courses/" + courseId
                + "/lessons/" + lessonId + "/edit";
    }

    @PostMapping("/my-courses/{courseId}/lessons/{lessonId}/delete")
    public String lessonDelete(
            @PathVariable int courseId,
            @PathVariable int lessonId,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            service.deleteLesson(courseId, user.getId(), lessonId);
            flash.addFlashAttribute("msg", "Lesson deleted.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("err", e.getMessage());
        }

        return "redirect:/my-courses/" + courseId + "/edit";
    }

    @PostMapping("/my-courses/{id}/modules")
    public String addModule(
            @PathVariable int id,
            @RequestParam(required = false) Integer parentId,
            @RequestParam String title,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            service.addSection(id, user.getId(), parentId, title);
            flash.addFlashAttribute("msg", "Module created.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("err", e.getMessage());
        }

        return "redirect:/my-courses/" + id + "/edit";
    }

    @PostMapping("/my-courses/{id}/modules/{moduleId}/rename")
    public String renameModule(
            @PathVariable int id,
            @PathVariable int moduleId,
            @RequestParam String title,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            service.renameSection(id, user.getId(), moduleId, title);
            flash.addFlashAttribute("msg", "Module renamed.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("err", e.getMessage());
        }

        return "redirect:/my-courses/" + id + "/edit";
    }

    @PostMapping("/my-courses/{id}/modules/{moduleId}/delete")
    public String deleteModule(
            @PathVariable int id,
            @PathVariable int moduleId,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            service.deleteSection(id, user.getId(), moduleId);
            flash.addFlashAttribute(
                    "msg",
                    "Module and its lessons deleted."
            );
        } catch (RuntimeException e) {
            flash.addFlashAttribute("err", e.getMessage());
        }

        return "redirect:/my-courses/" + id + "/edit";
    }

    @PostMapping("/my-courses/{id}/module-lessons")
    public String addModuleLesson(
            @PathVariable int id,
            @RequestParam int sectionId,
            @RequestParam String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String contentUrl,
            @RequestParam(defaultValue = "TEXT") String type,
            @RequestParam(defaultValue = "5") int minutes,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            service.addLessonToSection(
                    id, user.getId(), sectionId,
                    title, content, contentUrl, type, minutes
            );
            flash.addFlashAttribute("msg", "Lesson added.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("err", e.getMessage());
        }

        return "redirect:/my-courses/" + id + "/edit";
    }

    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> items = service.cart(user.getId());

        BigDecimal total = items.stream()
                .map(item -> new BigDecimal(String.valueOf(item.get("price"))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("currentUser", user);

        return "course/cart";
    }

    @PostMapping("/cart/{id}")
    public String addCart(
            @PathVariable int id,
            HttpSession session,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            service.addToCart(id, user.getId());
            flash.addFlashAttribute("msg", "Course added to your cart.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("err", e.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/cart/{id}/remove")
    public String removeCart(
            @PathVariable int id,
            HttpSession session) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        service.removeFromCart(id, user.getId());

        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model, HttpSession session) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> items = service.cart(user.getId());

        if (items.isEmpty()) {
            return "redirect:/cart";
        }

        BigDecimal total = items.stream()
                .map(item -> new BigDecimal(String.valueOf(item.get("price"))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("currentUser", user);

        return "course/checkout";
    }

    @PostMapping("/checkout")
    public String checkout(
            @RequestParam String paymentMethod,
            HttpSession session,
            Model model,
            RedirectAttributes flash) {

        User user = signedIn(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {
            Map<String, Object> order =
                    service.checkout(user.getId(), paymentMethod);

            model.addAttribute("order", order);
            model.addAttribute("currentUser", user);

            return "course/purchase-success";

        } catch (RuntimeException e) {
            flash.addFlashAttribute("err", e.getMessage());
            return "redirect:/cart";
        }
    }
}
