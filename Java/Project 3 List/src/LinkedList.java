public class LinkedList<T extends Comparable<T>> implements List<T> {
    private Node<T> head;
    private boolean isSorted;
    public LinkedList() {
        isSorted = true;
        head = new Node<T>(null);
    }

    public boolean add(T element) {
        // Check if the element is null
        if (element == null) {
            return false;
        }

        // Create a new node with the specified element
        Node<T> newNode = new Node<T>(element);

        // Check if the list is empty
        if (head.getNext() == null) {
            head.setNext(newNode);  // Set the new node as the first node
        } else {
            Node<T> currNode = head.getNext();

            // Traverse to the end of the list
            while (currNode.getNext() != null) {
                currNode = currNode.getNext();
            }

            // Check if the list is not sorted
            if (currNode.getData() != null && newNode.getData() != null &&
                    currNode.getData().compareTo(newNode.getData()) > 0) {
                isSorted = false;
            }

            // Add the new node to the end of the list
            currNode.setNext(newNode);
        }

        return true; // Element added successfully
    }

    public boolean add(int index, T element) {
        // Check if the index is out of bounds
        if (index < 0 || index > size() - 1) {
            return false;  // Invalid index
        }

        // Create a new node with the specified element
        Node<T> newNode = new Node<T>(element);
        Node<T> currNode = head;
        int currIndex = 0;

        // Move to the node before the target index
        while (currIndex < index) {
            // If the next node is null, insert a node with null data to fill the gap
            if (currNode.getNext() == null) {
                Node<T> nullNode = new Node<T>(null);
                nullNode.setNext(currNode.getNext());
                currNode.setNext(nullNode);
            } else if (currIndex == index - 1 && currNode.getNext().getData() == null) {
                // If the next node has null data, replace it with the new node
                currNode.getNext().setData(element);
                return true;
            }
            currNode = currNode.getNext();
            currIndex++;
        }

        // Insert the new node
        newNode.setNext(currNode.getNext());
        currNode.setNext(newNode);

        // Check and update isSorted flag after adding the element
        isSorted = checkIfSorted();

        return true; // Element added successfully
    }

    public void clear() {
        // Set the head's next reference to null, effectively clearing the entire list
        head.setNext(null);
        // Reset the isSorted flag to true, indicating an empty and sorted list
        isSorted = true;
    }

    public T get(int index) {
        // Check if the index is less than 0 (invalid index)
        if (index < 0) {
            return null;
        }

        // Initialize the current node to the first node in the list
        Node<T> currNode = head.getNext();
        // Initialize the current index to 0
        int currIndex = 0;

        // Iterate through the list until the end or the specified index is reached
        while (currNode != null && currIndex < index) {
            currNode = currNode.getNext(); // Move to the next node
            currIndex++; // Increment the current index
        }

        // Check if the current node is not null (index is within bounds)
        if (currNode != null) {
            return currNode.getData(); // Return the data of the current node
        } else {
            return null; // Return null if the index is out of bounds or the list is empty
        }
    }

    public int indexOf(T element) {
        // Initialize the current node to the first node in the list
        Node<T> currNode = head.getNext();
        // Initialize the current index to 0
        int currIndex = 0;

        // Check if the element is null
        if (element == null) {
            return -1; // Return -1 if the element is null (not found in the list)
        }

        // Iterate through the list
        while (currNode != null) {
            // Check if the current node's data is equal to the specified element
            if (element.equals(currNode.getData())) {
                return currIndex; // Return the current index if the element is found
            }
            currNode = currNode.getNext(); // Move to the next node
            currIndex++; // Increment the current index
        }

        // Return -1 if the element is not found in the list
        return -1;
    }

    public boolean isEmpty() {
        // Check if the next node after the head is null, indicating an empty list
        return head.getNext() == null;
    }

    public int size() {
        // Initialize a counter for the number of non-null elements
        int numElem = 0;

        // Start with the first node after the head
        Node<T> currNode = head.getNext();

        // Iterate through the list to count non-null elements
        while (currNode != null) {
            // Check if the current node's data is not null
            if (currNode.getData() != null) {
                // Increment the counter for non-null elements
                numElem++;
            }

            // Move to the next node
            currNode = currNode.getNext();
        }

        // Return the total number of non-null elements in the list
        return numElem;
    }

    public void sort() {
        // Check if the list is already sorted
        if (isSorted) {
            return;
        }

        // Initialize pointers for the sorted and unsorted portions of the list
        Node<T> sorted = null;
        Node<T> unsorted = head.getNext();

        // Iterate through the unsorted portion of the list
        while (unsorted != null) {
            // Save the reference to the next unsorted node
            Node<T> nextUnsorted = unsorted.getNext();

            // Check if the sorted list is empty or if the current unsorted node should be the new head
            if (sorted == null || (unsorted.getData() != null && sorted.getData().compareTo(unsorted.getData()) > 0)) {
                // Update the next reference of the current unsorted node to point to the sorted list
                unsorted.setNext(sorted);
                // Update the sorted list to include the current unsorted node as the new head
                sorted = unsorted;
            } else {
                // Initialize a pointer for traversing the sorted list
                Node<T> currNode = sorted;

                // Traverse the sorted list to find the correct position for the current unsorted node
                while (currNode.getNext() != null && (currNode.getNext().getData() == null || currNode.getNext().getData().compareTo(unsorted.getData()) < 0)) {
                    currNode = currNode.getNext();
                }

                // Update the next reference of the current unsorted node to insert it into the sorted list
                unsorted.setNext(currNode.getNext());
                // Update the next reference of the previous node in the sorted list to point to the current unsorted node
                currNode.setNext(unsorted);
            }

            // Move to the next unsorted node
            unsorted = nextUnsorted;
        }

        // Update the head of the list to point to the sorted list
        head.setNext(sorted);
        // Mark the list as sorted
        isSorted = true;
    }

    public T remove(int index) {
        // Check if the index is invalid
        if (index < 0 || index >= size()) {
            return null;
        }

        // Initialize pointers for traversing the list
        Node<T> currNode = head;
        int currIndex = 0;

        // Move to the node before the target index
        while (currNode.getNext() != null && currIndex < index) {
            currNode = currNode.getNext();
            currIndex++;
        }

        // Check if the next node exists
        if (currNode.getNext() != null) {
            // Save references to the node to be removed and the node after it
            Node<T> removeNode = currNode.getNext();
            Node<T> nextNode = removeNode.getNext();

            // Update the next reference of the current node to skip the removed node
            currNode.setNext(nextNode);

            // Check if the list is sorted
            isSorted = checkIfSorted();

            // Return the data of the removed node
            return removeNode.getData();
        }

        // Return null if the index is out of bounds
        return null;
    }

    public void equalTo(T element) {
        // Check if the element is null
        if (element == null) {
            return;
        }

        // Initialize a pointer for traversing the list
        Node<T> currNode = head;

        // Iterate through the list
        while (currNode.getNext() != null) {
            // Check if the list is sorted and the next node's data is not equal to the specified element
            if (isSorted && currNode.getNext().getData().compareTo(element) != 0) {
                // Skip the next node if it doesn't match the specified element
                currNode.setNext(currNode.getNext().getNext());
            } else if (!isSorted && !currNode.getNext().getData().equals(element)) {
                // Skip the next node if it doesn't match the specified element (unsorted list)
                currNode.setNext(currNode.getNext().getNext());
            } else {
                // Move to the next node if it matches the specified element or the list is sorted
                currNode = currNode.getNext();
            }
        }
        isSorted = true;
    }

    public void reverse() {
        // Initialize pointers for reversing the list
        Node<T> prev = null;
        Node<T> curr = head.getNext();
        Node<T> next;

        // Iterate through the list
        while (curr != null) {
            // Save the next node
            next = curr.getNext();

            // Reverse the link of the current node
            curr.setNext(prev);

            // Move to the next nodes
            prev = curr;
            curr = next;
        }

        // Check if reversal sorted the list
        isSorted = checkIfSorted();

        // Update the head to point to the new first node (which was the last node)
        head.setNext(prev);
    }

    public void intersect(List<T> otherList) {
        // Check for null or empty lists
        if (otherList == null || otherList.isEmpty() || head == null) {
            return;
        }

        // Cast 'otherList' to LinkedList for access to its methods
        LinkedList<T> other = (LinkedList<T>) otherList;

        // Pointers to traverse the current list and the 'otherList'
        Node<T> currNode = head.getNext();
        Node<T> otherCurrNode = other.head.getNext();

        // Create a temporary list to store the intersection
        LinkedList<T> temp = new LinkedList<>();

        // Sort the current list (if not already sorted) and set isSorted to true
        sort();

        // Iterate over the elements in both lists
        while (currNode != null && otherCurrNode != null) {
            // Check if the element is present in both lists
            if (currNode.getData() != null && otherCurrNode.getData() != null && currNode.getData().equals(otherCurrNode.getData())) {
                temp.add(currNode.getData());
                currNode = currNode.getNext();
                otherCurrNode = otherCurrNode.getNext();
            } else if (currNode.getData() == null || currNode.getData().compareTo(otherCurrNode.getData()) < 0) {
                currNode = currNode.getNext();
            } else {
                otherCurrNode = otherCurrNode.getNext();
            }
        }

        // Update the current list with the intersection stored in 'temp'
        clear(); // Clear the current list
        Node<T> tempCurrNode = temp.head.getNext();
        while (tempCurrNode != null) {
            add(tempCurrNode.getData()); // Add each element from 'temp' to the current list
            tempCurrNode = tempCurrNode.getNext();
        }
    }

    public T getMin() {
        // Check if the list is empty
        if (head.getNext() == null) {
            return null; // Handle the case where the list is empty
        }

        // Initialize variables to track the minimum value
        Node<T> currNode = head.getNext();
        T min = currNode.getData();

        // If the list is sorted, the minimum is the first element
        if (isSorted) {
            return min;
        } else {
            // If the list is not sorted, iterate over the list to find the minimum
            while (currNode != null) {
                if (currNode.getData().compareTo(min) < 0) {
                    min = currNode.getData();
                }
                currNode = currNode.getNext();
            }
            return min;
        }
    }

    public T getMax() {
        // Check if the list is empty
        if (head.getNext() == null) {
            return null; // Handle the case where the list is empty
        }

        // Initialize variables to track the maximum value
        Node<T> currNode = head.getNext();
        T max = currNode.getData();

        // If the list is sorted, the maximum is the last element
        if (isSorted) {
            while (currNode.getNext() != null) {
                currNode = currNode.getNext();
                max = currNode.getData();
            }
        } else {
            // If the list is not sorted, iterate over the list to find the maximum
            while (currNode != null) {
                if (currNode.getData().compareTo(max) > 0) {
                    max = currNode.getData();
                }
                currNode = currNode.getNext();
            }
        }
        return max;
    }

    public String toString() {
        // StringBuilder to construct the string representation
        StringBuilder strBuild = new StringBuilder();

        // Start from the first node in the list
        Node<T> currNode = head.getNext();

        // Iterate over the nodes in the list
        while (currNode != null && currNode.getData() != null) {
            // Append the data of the current node to the string
            strBuild.append(currNode.getData());

            // Append a newline character to separate elements
            strBuild.append("\n");

            // Move to the next node
            currNode = currNode.getNext();
        }

        // Convert the StringBuilder to a String and return
        return strBuild.toString();
    }

    public boolean isSorted() {
        return isSorted;
    }

    // Helper method to check if the linked list is sorted
    private boolean checkIfSorted() {
        // If the list is empty or has only one element, it is considered sorted
        if (head.getNext() == null || head.getNext().getNext() == null) {
            return true;
        }

        // Initialize pointers for traversing the list
        Node<T> currNode = head.getNext();
        Node<T> nextNode = currNode.getNext();

        // Iterate through the list
        while (nextNode != null) {
            // Check if the current node's data is greater than the next node's data
            if (currNode.getData() != null && nextNode.getData() != null && currNode.getData().compareTo(nextNode.getData()) > 0) {
                return false; // The list is not sorted
            }

            // Move to the next nodes
            currNode = nextNode;
            nextNode = nextNode.getNext();
        }

        return true; // The list is sorted
    }
}

// Written by Ayub Mohamoud, moha1660