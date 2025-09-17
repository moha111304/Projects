public class ArrayList<T extends Comparable<T>> implements List<T> {
    private boolean isSorted;
    private T[] list;
    public ArrayList() {
        this.list = (T[]) new Comparable[2];
        isSorted = true;
    }

    public boolean add(T element) {
        // Check for null element
        if (element == null) {
            return false; // Null element found
        }

        int currentSize = size();  // Store the size in a variable

        // Check if the list needs to be resized
        if (currentSize == this.list.length) {
            // Grow and copy array if it's full
            this.grow();
        }

        this.list[currentSize] = element;

        // Check if the added element breaks the sorted order
        if (isSorted) {
            // Check the last two elements to determine if the list is still sorted
            if (currentSize > 0 && this.list[currentSize - 1].compareTo(this.list[currentSize]) > 0) {
                isSorted = false; // The list is not sorted
            }
        }

        return true; // Element added successfully
    }

    public boolean add(int index, T element) {
        // Check for null element or invalid index
        if (element == null || index < 0 || index > size()) {
            return false; // Invalid index or null element, operation failed
        }

        int currentSize = size();

        // Grow and copy array if it's out of bound or full
        if (currentSize == this.list.length) {
            this.grow();
        }

        // Shift elements to make room for the new element
        for (int i = currentSize; i > index; i--) {
            this.list[i] = this.list[i - 1];
        }

        this.list[index] = element;

        // Update isSorted after adding the element
        if (isSorted) {
            isSorted = checkIfSorted();
        }

        return true; // Element added successfully
    }

    public void clear() {
        for (int i = 0; i < this.list.length; i++) {
            this.list[i] = null; // Set all elements in array to null
        }
        isSorted = true;
    }

    public T get(int index) {
        if (index < 0 || index >= size()) {
            return null; // Return null if the index is out of bounds
        }

        return this.list[index]; // Returns element at the index
    }

    public int indexOf(T element){
        if (element == null) {
            return -1; // Element is null
        }

        if (isSorted) {
            for (int i = 0; i < this.list.length; i++) {
                if (this.list[i] != null && this.list[i].compareTo(element) > 0) {
                    break;
                }
                if (this.list[i] != null && this.list[i].equals(element)) {
                    return i; // Returns the index of the specified element
                }
            }
        } else {
            for (int i = 0; i < this.list.length; i++) {
                if (this.list[i] != null && this.list[i].equals(element)) {
                    return i; // Returns the index of the specified element
                }
            }
        }
        return -1; // Element not found
    }

    public boolean isEmpty() {
        if (this.list == null) {
            return true; // Array is null, consider it as empty
        }

       if (size() == 0) {
           return true; // No non-null elements
       }
       return false; // There is at least 1 non-null element
    }

    public int size() {
        if (this.list == null) {
            return 0;  // Array is null, so size is 0
        }

        int size = 0;
        for (T t : this.list) {
            if (t != null) {
                size++; // If element is not null, increment size
            }
        }

        return size;
    }

    public void sort() {
        if (isSorted) {
            return; // Only sort if the list is not already sorted
        }
            T n;

            // Iterate over the unsorted part of the list
            for (int i = 1; i < size(); i++) {
                n = this.list[i];
                int j = i - 1;

                // Shift elements in the sorted part to make room for the current element
                while (j >= 0 && n.compareTo(this.list[j]) < 0) {
                    this.list[j + 1] = this.list[j];
                    j = j - 1;
                }

                // Insert the current element into its correct position in the sorted part
                this.list[j + 1] = n;
            }

            // Mark the list as sorted
            isSorted = true;
    }

    public T remove(int index) {
        // Check for invalid index or empty list
        if (index < 0 || index >= this.list.length || isEmpty()) {
            return null;  // Invalid index or empty list
        }

        // Store the element to be removed
        T temp = this.list[index];

        // Shift subsequent elements to the left
        for (int i = index; i < size() - 1; i++) {
            this.list[i] = this.list[i + 1];
        }


        // Set the last element to null and decrement the size
        this.list[size() - 1] = null;

        // Check if removal sorted the list
        if (!isSorted) {
            isSorted = checkIfSorted();
        }

        return temp; // Return the removed element
    }

    public void equalTo(T element) {
        // Check for null element
        if (element == null) {
            return;
        }

        // Check if the list is sorted
        if (isSorted) {
            // Iterate over the elements in the sorted list
            for (int i = 0; i < this.list.length; i++) {
                // Skip null elements
                if (this.list[i] == null) {
                    continue;
                }

                // Compare the current element with the specified element
                int comparison = this.list[i].compareTo(element);

                // If not equal, set the element to null
                if (comparison != 0) {
                    this.list[i] = null;
                }

                if (comparison > 0) {
                    break;
                }
            }
        } else {
            // Iterate over the elements in the unsorted list
            for (int i = 0; i < this.list.length; i++) {
                // Remove elements not equal to the specified element
                if (this.list[i] != null && !this.list[i].equals(element)) {
                    this.list[i] = null;
                }
            }
        }

        // Count the number of elements in 'this.list' that are equal to the specified 'element'
        int numElem = 0;
        for (T t : this.list) {
            if (t == null) {
                continue;
            }
            if (t.equals(element)) {
                numElem++;
            }
        }

        // Initialize an index for traversing the list
        int index = 0;

        // Iterate through the list to replace the first 'numElem' elements with 'element'
        // and set the remaining elements to null
        for (T t : this.list) {
            // Check if the index is less than the count of elements to be replaced
            if (index < numElem) {
                // Replace the current element with 'element'
                this.list[index] = element;
                index++;
            } else {
                // Set the remaining elements to null
                this.list[index] = null;
                index++;
            }
        }

        isSorted = true;
    }

    public void reverse() {
        int left = 0;
        int right = size() - 1;

        while (left < right) {
            // Swap elements at the left and right indices
            T temp = get(left);
            this.list[left] = get(right);
            this.list[right] = temp;

            // Move indices towards the center
            left++;
            right--;
        }

        // Check if the list is sorted
        isSorted = checkIfSorted();
    }

    public void intersect(List<T> otherList) {
        // Check for null or empty otherList
        if (otherList == null || otherList.isEmpty()) {
            return;
        }

        // Sort the current list and set isSorted to true
        sort();

        // Create a temporary array to store the intersection
        T[] temp = (T[]) new Comparable[this.list.length];

        // Index to keep track of the next available position in the temp array
        int tempIndex = 0;

        // Iterate over the elements in the current list
        for (T t : this.list) {
            // Iterate over the elements in the other list
            for (int i = 0; i < otherList.size(); i++) {
                // Check if both elements are non-null and equal
                if (t != null && otherList.get(i) != null && t.equals(otherList.get(i))) {
                    // Add the element to the temporary array
                    temp[tempIndex++] = t;
                    break;
                }
            }
        }

        // Clear the current list
        clear();

        // Set list to temporary array
        this.list = temp;
    }

    public T getMin() {
        // Check if the list is empty
        if (this.list.length == 0) {
            return null;
        }

        // If the list is sorted, return the first non-null element
        if (isSorted) {
            for (T t : this.list) {
                if (t != null) {
                    return t;
                }
            }
        } else {
            // If the list is not sorted, find the minimum element through iteration
            T min = null;  // Initialize min to null
            for (T t : this.list) {
                if (t != null && (min == null || t.compareTo(min) < 0)) {
                    min = t;
                }
            }
            return min;
        }

        return null;  // Default case if the list is neither empty nor sorted
    }

    public T getMax() {
        // If the list is sorted, return the last non-null element
        if (isSorted) {
            // Check if the list is full, return the last element
            if (size() == this.list.length) {
                return this.list[size() - 1];
            } else {
                // Iterate in reverse to find the last non-null element
                for (int i = this.list.length - 1; i >= 0; i--) {
                    if (this.list[i] != null) {
                        return this.list[i];
                    }
                }
            }
        } else {
            // If the list is not sorted, find the maximum element through iteration
            T max = null;
            for (T t : this.list) {
                if (t != null && (max == null || t.compareTo(max) > 0)) {
                    max = t;
                }
            }
            return max;
        }

        // Return null if the list is empty or contains only null elements
        return null;
    }

    public String toString() {
        // StringBuilder to construct the string representation
        StringBuilder strBuild = new StringBuilder();

        // Iterate through the list and append non-null elements to the StringBuilder
        for (T t : this.list) {
            if (t != null) {
                strBuild.append(t);
                strBuild.append("\n");
            }
        }

        // Convert the StringBuilder to a String and return
        return strBuild.toString();
    }

    public boolean isSorted() {
        return isSorted;
    }

    // Helper to grow and copy the array
    private void grow() {
        // Create a new array with double the capacity of the current array
        T[] newArray = (T[]) new Comparable[this.list.length * 2];

        // Copy elements from the old array to the new array
        for (int i = 0; i < this.list.length; i++) {
            newArray[i] = this.list[i];
        }

        // Set list to newArray
        this.list = newArray;
    }

    // Helper method to check if the list is sorted
    private boolean checkIfSorted() {
        for (int i = 0; i < size() - 1; i++) {
            T currentElement = this.list[i];
            T nextElement = this.list[i + 1];

            // Skip null elements during comparison
            if (currentElement == null || nextElement == null) {
                continue;
            }

            // Check if the current element is greater than the next element
            if (currentElement.compareTo(nextElement) > 0) {
                return false; // Not sorted
            }
        }
        return true; // Sorted
    }
}

// Written by Ayub Mohamoud, moha1660