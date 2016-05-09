package threads

import java.io._
import java.util.concurrent._
import java.nio.ByteBuffer
import scalax.collection.Graph
import scala.util.control.Breaks._
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._
import language.implicitConversions
import scalax.collection.edge.LDiEdge
import scalax.collection.io.dot._
import scalax.collection.edge.Implicits._
import Indent._
import implicits._
import javax.sound.midi.SysexMessage
import scalax.collection.GraphTraversal._
import scalax.collection.GraphTraversalImpl._
import scalax.collection.mutable.SimpleArraySet


//The following class will serve as the edge of my graph
//The property color will represent which thread has resulted in this edge
class ThreadEdge[N](src: N, dst: N, val color: String)
        extends DiEdge[N](NodeProduct(src, dst))
        with    ExtendedKey[N]
        with    EdgeCopy[ThreadEdge]
        with    OuterEdge[N,ThreadEdge] {
     
      def keyAttributes = Seq(color)
       def this(nodes: Product, color: String ) 
     {       
           this(nodes.productElement(0).asInstanceOf[N],nodes.productElement(1).asInstanceOf[N],color)
     }
      override def copy[NN](newNodes: Product) =
        new ThreadEdge[NN](newNodes, color)
    }
    object ThreadEdge {
      
      def apply[N,L](src: N,dst: N,  color: String) =
        new ThreadEdge[N](NodeProduct(src, dst),color )
        
      def unapply[N](e: ThreadEdge[N]): Option[(N,N,String)] = Some((e._1, e._2, e.color))
    }
    
    
object Threads {
  def main(args: Array[String]) {
    
 
    //Method to change bytes to binary
    def toBinary(i: Int, digits: Int = 8) =
    String.format("%" + digits + "s", i.toBinaryString).replace(' ', '0')
    
    //Hash Table to store state values
    var ByteHash:Map[ByteBuffer,Array[Byte]] = Map()
    
    var bit: Array[Int] = Array(0, 0, 0)
    var byte: Array[Byte] = Array(0.toByte, 0.toByte, 0.toByte)
    
    //Totalcount will keep track of total number of bugs in the graph when we plant them
    var Totalcount = 0;
   
    var Bugs1 = 0;//Count of Buggy states from random walk
    var Bugs2 = 0; //Count of Buggy states from random + bounded BFS
    
    println("Initial Bytes:"+toBinary(byte(0),8)+" "+toBinary(byte(1),8)+" "+toBinary(byte(1),8));
    
    //z will store how many times each thread is called
    var z = new Array[Int](51);
    for(u<-1 to 50)
      z(u) = 0;
  
    val g =  scalax.collection.mutable.Graph.empty[String,ThreadEdge] 
    
    val r = new scala.util.Random

    //Method to find a random next node -- for Random Walk
    //Before calling this function, always make sure the node has a next node.
       def randomSuccessor(node: g.NodeT): g.NodeT =
        node.edges.filter((e: g.EdgeT) => e.hasTarget((n: g.NodeT) => n ne node)) match {
          case set: SimpleArraySet[g.EdgeT] => set.draw(r).targets.filter(_ ne node).head
        }
    
    //Convert each byte to string, concatenate them and add to the graph as an initial node
    var str = byte(0).toString() +" " + byte(1).toString() +" "+ byte(2).toString();
    g.add(str);
  
     class ThreadCreate(i:Int,bits: Array[Int], bytes: Array[Byte]) extends Runnable {    
    
         def run {
             var startTime = System.currentTimeMillis();
             while(System.currentTimeMillis()<startTime + 1200000)
             {
              val r = new scala.util.Random
              val rnd = r.nextInt(5000);
              var str1 = "";
              var insert = 0;
              Thread.sleep(rnd);
              
              //Toggle a random bit among the 3 bytes
              bits((i-1)%3) ^= 1 << (((i-1)/3)%8);
              bytes((i-1)%3) = bits((i-1)%3).toByte;
              println("Thread"+i+":"+toBinary(bits(0),8)+" "+toBinary(bits(1),8)+" "+toBinary(bits(2),8));
              println("Bytes"+i+":"+(bytes(0)+" "+bytes(1)+" "+bytes(2)));
              
              //In every 100 nodes introduce a bug. A bug is represented by a '*' in place of a bit.
              if(g.order % 100 == 0)
              {
                insert = 0;
                 val x = new scala.util.Random
                 val rnd = x.nextInt(3);
                 val x1 = new scala.util.Random
                 val rnd1 = x1.nextInt(8);
                 rnd match{
                   case 0 => 
                             val bit = toBinary(bits(0),8);
                             val charArray = bit.toCharArray();
                             charArray(rnd1) = '*';
                             str1 = new String(charArray) +" " + byte(1).toString() +" "+ byte(2).toString();
                   case 1 => 
                             val bit = toBinary(bits(1),8);
                             val charArray = bit.toCharArray();
                             charArray(rnd1) = '*';
                             str1 = byte(0).toString() +" " + new String(charArray) +" "+ byte(2).toString();
                   case 2 => 
                             val bit = toBinary(bits(2),8);
                             val charArray = bit.toCharArray();
                             charArray(rnd1) = '*';
                             str1 = byte(0).toString() +" " + byte(1).toString() +" "+ new String(charArray);
                 }
                 Totalcount = Totalcount + 1;
                 
              }
              else
              {
                insert = 1;
                 str1 = byte(0).toString() +" " + byte(1).toString() +" "+ byte(2).toString();
              }
              
              z(i) = z(i) +1;
              
              //Add the edge to the graph
              g.add(new ThreadEdge(str,str1,i.toString()));
              
              str = str1;
              
              //Add to hash table
              if(!ByteHash.contains(ByteBuffer.wrap(bytes)) && insert == 1)
                    ByteHash += (ByteBuffer.wrap(bytes) -> bytes);
              else if (insert == 1)
              {
                //yield if key is already present in the hash table
                Thread.`yield`();
              }
              
              
             }
}
   }
     
   
   
   
     for (i <- 1 to 50) 
     {
        var thread = new Thread(new ThreadCreate(i,bit,byte));
        thread.start;
     }
     Thread.sleep(1204000);
    
     
     
     //Convert the graph to DOT format
    
      val root = DotRootGraph (
    directed = true,
    id        = Some("MyDot"));
                     
  def edgeTransformer(innerEdge: Graph[String,ThreadEdge]#EdgeT):
    Option[(DotGraph,DotEdgeStmt)] = innerEdge.edge match {                   
                     case ThreadEdge(source, target,color) => color match {
                          case color: String =>
                                  Some((root,
                                             DotEdgeStmt(source.toString,
                                                                  target.toString,
                                                                  if (color !=null ) List(DotAttr("label", color.toString))
                                                                  else                Nil)))
                     
                     }}
  
  //Implementing the walks to find bugs
     var n1 = g get "0 0 0";
     

     
     //Implement bounded BFS with a bound of 5 nodes
      for(h<-0 to 5)
      {
        n1 = g get "0 0 0";
     while((n1 findSuccessor (_.edges forall (_.directed))) != None)
     {
         val builder = g.newWalkBuilder(n1);
         val n2= randomSuccessor(n1);         
         builder.add(n2); 
         n1 = n2;
         if(n1.value contains "*")
         {
           Bugs1 = Bugs1+1;
         }
     }
      }
     
         import g.ExtendedNodeVisitor
         
         var c = 1;
         n1 = g get "0 0 0";
         n1.innerNodeTraverser.withKind(BreadthFirst).foreach {
         ExtendedNodeVisitor((node, count, depth, informer) => {
           
             if((node.value contains "*") && count<1000 && c>1)
             {
                Bugs2 = Bugs2+1;
             }
             if(depth == 100)
             {
                var n2 = node;
                while((n2 findSuccessor (_.edges forall (_.directed))) != None)
                {
                   val builder = g.newWalkBuilder(n2);
                   val n3= randomSuccessor(n2);         
                   builder.add(n3); 
                   n2 = n3;
                   if(n2.value contains "*")
                   {
                     Bugs2 = Bugs2+1;
                   }
                }
             }
                
             
               
           c=c+1;
          })

      }
      
      
      
    //Implementing BDFS
      import g.ExtendedNodeVisitor
              
        
      n1 = g get "0 0 0";
      var Bugs3 = 0;
         n1.innerNodeTraverser.withKind(DepthFirst).foreach {
         ExtendedNodeVisitor((node, count, depth, informer) => {
             //info :+= (node.value, count)
             if((node.value contains "*") && count<1000)
             {
               //println("node:"+node.value); 
                Bugs3 = Bugs3+1;
                              
             }
             else if(count==7000)
             {
               for(x <- 0 to 1)
               {
               var n3 = g get node;
               while((n3 findSuccessor (_.edges forall (_.directed))) != None)
               {
                 val builder = g.newWalkBuilder(n3);
                 val n2= randomSuccessor(n3);         
                 builder.add(n2); 
                 n3 = n2;
                 if(n3.value contains "*")
                 {
                   Bugs3 = Bugs3 + 1;
                 }
             }
             }
               
             }
          })
         }
       
      
         
     //Write the DOT graph to a file named test.dot for analysis
         val writer = new PrintWriter(new File("test.dot" ))
         writer.write(g.toDot(root,edgeTransformer))
         writer.close()
         println("Number of nodes = "+ g.order);
         println("Number of edges = "+ g.graphSize);
         println("Total Number of bugs " + Totalcount);
         println("Buggy states from random walk " + Bugs1);
         println("Buggy states from random walk + bounded BFS " + Bugs2/3);
         println("Number of states that might violate liveness " + Bugs3);
         
      
  }
}