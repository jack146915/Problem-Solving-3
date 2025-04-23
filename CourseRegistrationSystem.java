import java.util.*;

public class CourseRegistrationSystem {
    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, Course> courses = new HashMap<>();
    private final Set<String> loggedInStudents = new HashSet<>();

    // --- Add Student ---
    public void addStudent(String studentId, String name) {
        assert !students.containsKey(studentId) : "Pre-condition failed: Student already exists";
        students.put(studentId, new Student(studentId, name));
        loggedInStudents.add(studentId); // Simulate login
    }

    // --- Add Course ---
    public void addCourse(String courseId, String title, String time, int capacity) {
        assert !courses.containsKey(courseId) : "Pre-condition failed: Course already exists";
        courses.put(courseId, new Course(courseId, title, time, capacity));
    }

    // --- Register a student to a course ---
    public void registerCourse(String studentId, String courseId) {
        assert loggedInStudents.contains(studentId) : "Pre-condition: Student must be logged in";
        assert courses.containsKey(courseId) : "Pre-condition: Course must exist";

        Course course = courses.get(courseId);
        Student student = students.get(studentId);

        assert !student.getCourseIds().contains(courseId) : "Pre-condition: Already registered";
        assert course.enrolled < course.capacity : "Pre-condition: Course full";

        // Invariant: No time clash
        for (String cid : student.getCourseIds()) {
            Course c = courses.get(cid);
            assert !c.time.equals(course.time) : "Invariant violated: Time clash with " + cid;
        }

        course.enrolled++;
        student.addCourse(courseId);

        assert student.getCourseIds().contains(courseId) : "Post-condition: Course not added to student";
        checkInvariant();

        System.out.println("[SUCCESS] " + studentId + " registered to " + courseId);
    }

    // --- View course details ---
    public Map<String, String> viewCourseDetails(String studentId, String courseId) {
        assert loggedInStudents.contains(studentId) : "Pre-condition: Student must be logged in";
        assert courses.containsKey(courseId) : "Pre-condition: Course must exist";

        Course course = courses.get(courseId);
        Map<String, String> details = new HashMap<>();
        details.put("name", course.name);
        details.put("details", course.details);
        details.put("status", course.enrolled + "/" + course.capacity);

        assert details.containsKey("name") && details.containsKey("details") : "Post-condition: Incomplete details";
        checkInvariant();
        return details;
    }

    // --- Drop course ---
    public void dropCourse(String studentId, String courseId) {
        assert loggedInStudents.contains(studentId) : "Pre-condition: Student must be logged in";
        assert courses.containsKey(courseId) : "Pre-condition: Course must exist";
        assert students.get(studentId).getCourseIds().contains(courseId) : "Pre-condition: Not enrolled in course";

        Course course = courses.get(courseId);
        Student student = students.get(studentId);
        int original = course.enrolled;

        course.enrolled--;
        student.removeCourse(courseId);

        assert !student.getCourseIds().contains(courseId) : "Post-condition: Course not removed";
        assert course.enrolled == original - 1 : "Post-condition: Enrollment count incorrect";
        checkInvariant();

        System.out.println("[SUCCESS] " + studentId + " dropped " + courseId);
    }

    // --- Invariant Check ---
    private void checkInvariant() {
        for (Course c : courses.values()) {
            assert c.enrolled <= c.capacity : "Invariant violated: Enrolled > Capacity";
        }
    }

    // --- Student Class ---
    static class Student {
        private final String id;
        private final String name;
        private final List<String> courseIds;

        public Student(String id, String name) {
            this.id = id;
            this.name = name;
            this.courseIds = new ArrayList<>();
        }

        public void addCourse(String courseId) {
            courseIds.add(courseId);
        }

        public void removeCourse(String courseId) {
            courseIds.remove(courseId);
        }

        public List<String> getCourseIds() {
            return courseIds;
        }
    }

    // --- Course Class ---
    static class Course {
        String name;
        String details;
        String time;
        int capacity;
        int enrolled;

        public Course(String id, String title, String time, int capacity) {
            this.name = title;
            this.details = "Details for " + title;
            this.time = time;
            this.capacity = capacity;
            this.enrolled = 0;
        }
    }

    // --- Main Execution ---
    public static void main(String[] args) {
        CourseRegistrationSystem system = new CourseRegistrationSystem();

        system.addStudent("A22EC4000", "Ali");

        system.addCourse("SECJ2203", "Software Engineering", "Mon 9AM", 2);
        system.addCourse("SECR2043", "Operating Systems", "Mon 10AM", 2); // Same time to trigger conflict
        system.addCourse("SECD2523", "Database Systems", "Tue 10AM", 2);

        // Successful registration
        system.registerCourse("A22EC4000", "SECJ2203");

        // View course details
        Map<String, String> details = system.viewCourseDetails("A22EC4000", "SECJ2203");
        System.out.println("Course Details: " + details);

        // Uncomment to trigger time conflict invariant
        // system.registerCourse("S001", "CS102");

        // Drop course
        system.dropCourse("A22EC4000", "SECJ2203");
    }
}
