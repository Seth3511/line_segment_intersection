import java.util.TreeSet;
import java.util.*;
import java.io.*;

public class IntersectionFinder {
    private PriorityQueue<Event> queue;
    private TreeSet<SpacialObject> BST;
    private ArrayList <Point> intersections;
    private ArrayList <LineSegment>lines;
    private double xMax;
    private double yMax;

    public IntersectionFinder(String filename) throws IOException {
        queue = new PriorityQueue<>();
        BST = new TreeSet<>();
        intersections = new ArrayList<>();
        lines = new ArrayList<>();

        Event e;
        LineSegment s;
        Point p1, p2;
        Double x, y;
        Scanner f = new Scanner(new File(filename));
        xMax = 0;
        yMax = 0;

        while (f.hasNextLine()) {
            x = f.nextDouble();
            if (x > xMax)
                xMax = x;
            y = f.nextDouble();
            if (y > yMax)
                yMax = y;
            p1 = new Point(x, y);
            x = f.nextDouble();
            if (x > xMax)
                xMax = x;
            y = f.nextDouble();
            if (y > yMax)
                yMax = y;
            f.nextLine();
            p2 = new Point(x, y);

            s = new LineSegment(p1, p2);
            lines.add(s);
            queue.add(new Event(p1));
            queue.add(new Event(p2));
        }
        f.close();
    }

    public ArrayList find() {
        while (queue.size() > 0) {
            Event e = queue.poll();
            handleEvent(e);
        }
        return intersections;

        /*for(int i=0;i<lines.size()-1;i++)
            for(int j=i+1;j<lines.size();j++){
                LineSegment l=lines.get(i);
                LineSegment r=lines.get(j);
                Point p=findIntersect(l,r);

                if(p!=null){
                    if(p.y<=l.p1.y&&p.y<=r.p1.y&&p.y>=l.p2.y&&p.y>=r.p2.y)
                        if(!intersections.contains(p))
                            intersections.add(p);
                }
            }
        return intersections;*/
    }

    public void handleEvent(Event e) {
        //System.out.println(e.p.x+", "+e.p.y);
        LineSegment.currY=e.p.y;
        LineSegment.currX=e.p.x;
        ArrayList <LineSegment>segments = unionOfAll(e.p);
        if (segments.size() > 1) {
            if(!intersections.contains(e.p)){
                intersections.add(e.p);
                addIntersections(segments,e.p);
            }
        }

        deleteLUC(segments, e.p);
        LineSegment.prevY=LineSegment.currY;
        LineSegment.prevX=LineSegment.currX;
        ArrayList <LineSegment> UUC= addUUC(lines, e.p);

        if (UUC.size() == 0) {
            LineSegment s1, s2;

            s1 = (LineSegment) BST.lower(e.p);
            s2 = (LineSegment) BST.higher(e.p);

            if(s1!=null&&s2!=null){
                /*if((Math.abs(s1.slope)<.0000001))
                    findHorizontalIntersections(s1,e.p);
                else if((Math.abs(s2.slope)<.0000001))
                    findHorizontalIntersections(s2,e.p);
                else*/
                    findNewEvent(s1, s2, e.p);
            }
        } else {
            LineSegment s1, s2;

            s1 = UUC.get(UUC.size()-1);
            s2 = (LineSegment) BST.higher(s1);

            if(s1!=null&&s2!=null) {
                /*if((Math.abs(s1.slope)<.0000001))
                    findHorizontalIntersections(s1,e.p);
                else if((Math.abs(s2.slope)<.0000001))
                    findHorizontalIntersections(s2,e.p);
                else*/
                    findNewEvent(s1, s2, e.p);

            }
            s1 = UUC.get(0);
            s2 = (LineSegment) BST.lower(s1);

            if(s1!=null&&s2!=null){
                /*if((Math.abs(s1.slope)<.0000001))
                    findHorizontalIntersections(s1,e.p);
                else if((Math.abs(s2.slope)<.0000001))
                    findHorizontalIntersections(s2,e.p);
                else */
                    findNewEvent(s1, s2, e.p);
            }
        }
    }

    public void addIntersections(ArrayList <LineSegment> segments, Point p) {
        for (int i = 0; i < segments.size(); i++) {
            if(!segments.get(i).isIntersection(p))
                segments.get(i).intersections.add(p);
        }
    }

    public void findNewEvent(LineSegment l, LineSegment r, Point p) {
        Point point = null;
        if (l != null && r != null)
            point = findIntersect(l, r);
        if (point == null)
            return;
        if (point.y > l.p1.y || point.y < l.p2.y || point.y > r.p1.y || point.y < r.p2.y)
            return;
        else if(point.y<p.y||(point.y==p.y&&point.x>p.x)){
            if(!l.isIntersection(point))
                l.intersections.add(point);
            if(!r.isIntersection(point))
                r.intersections.add(point);
            if(!intersections.contains(point)){
                if(!queue.contains(new Event(p)))
                    queue.add(new Event(point));
                intersections.add(point);
            }
        }
    }

    public Point findIntersect(LineSegment l, LineSegment r) {
        if ((Math.abs(l.antislope-0)<.0000001) || (Math.abs(r.antislope-0)<.0000001)) {
            if ((Math.abs(l.antislope-0)<.0000001)&&(Math.abs(r.antislope-0)<.0000001))
                return null;
            else if((Math.abs(l.slope-0)<.0000001)||(Math.abs(r.slope-0)<.0000001)){

                if(Math.abs(l.slope-0)<.0000001){
                    double x=r.p1.x;
                    double y=l.p1.y;
                    return new Point(x,y);
                }
                else{
                    double x=l.p1.x;
                    double y=r.p1.y;
                    return new Point(x,y);
                }
            }
            else{
                double m1 = l.antislope;
                double b1 = l.xInt;
                double m2 = r.antislope;
                double b2 = r.xInt;

                if (Math.abs(m1-m2)<.0000001)
                    return null;

                double y = (b2 - b1) / (m1 - m2);
                double x = (m1 * ((b2 - b1) / (m1 - m2))) + b1;
                return new Point(x, y);
            }
        }

        else {
            double m1 = l.slope;
            double b1 = l.yInt;
            double m2 = r.slope;
            double b2 = r.yInt;

            if (Math.abs(m1-m2)<.0000001)
                return null;

            double x = (b2 - b1) / (m1 - m2);
            double y = (m1 * ((b2 - b1) / (m1 - m2))) + b1;
            return new Point(x, y);
        }
    }

    public ArrayList unionOfAll(Point p) {
        ArrayList<LineSegment> union = new ArrayList<>();
        ArrayList<LineSegment> set = new ArrayList<>();
        if (BST.size() > 0)
            set.addAll((Collection<? extends LineSegment>) (Collection<? extends SpacialObject>)BST.headSet(BST.last(), true));

        for (int i = 0; i < set.size(); i++)
            if (set.get(i).contains(p))
                union.add(set.get(i));

        return (union);
    }

    public void deleteLUC(ArrayList<LineSegment> segments, Point p) {
        ArrayList<LineSegment> set = new ArrayList<>();
        set.addAll((Collection<? extends LineSegment>) (Collection<? extends SpacialObject>)BST.descendingSet());
        for (int i = 0; i < segments.size(); i++)
            if (segments.get(i).isIntersection(p) || segments.get(i).isLower(p))
                set.remove(segments.get(i));
        BST=new TreeSet<>();
        BST.addAll(set.subList(0,set.size()));
    }

    public ArrayList<LineSegment> addUUC(ArrayList<LineSegment> segments, Point p) {
        ArrayList<LineSegment> UUC = new ArrayList<>();
        //ArrayList<LineSegment> set = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++)
            if (segments.get(i).isIntersection(p) || segments.get(i).isUpper(p)) {
                BST.add(segments.get(i));
                UUC.add(segments.get(i));
            }

        /*set.addAll((Collection<? extends LineSegment>) (Collection<? extends SpacialObject>)BST.descendingSet());
        for(int i=0;i<set.size();i++){
            LineSegment line=set.get(i);
            System.out.println("("+line.p1.x+", "+line.p1.y+")-("+line.p2.x+", "+line.p2.y+")");
        }
        System.out.println("");*/
        System.out.println(BST.size());
        UUC.sort(null);
        return UUC;
    }

    /*public void findHorizontalIntersections(LineSegment s, Point p){
        ArrayList <LineSegment> segments=new ArrayList<>();
        segments.addAll((Collection<? extends LineSegment>) (Collection<? extends SpacialObject>)BST.headSet(BST.last(), true));
        segments.sort(null);

        for(int i=0;i<segments.size();i++){
            if(!segments.get(i).equals(s))
                findNewEvent(s,segments.get(i),p);
        }
    }*/

    public ArrayList getLines() {
        return lines;
    }

    public double getXMax() {
        return xMax;
    }

    public double getYMax() {
        return yMax;
    }
}