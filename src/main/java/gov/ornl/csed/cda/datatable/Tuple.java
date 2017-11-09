package gov.ornl.csed.cda.datatable;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;

public class Tuple implements Serializable {
	private ArrayList<Double> elements = new ArrayList<>();
	private Instant instant;

	private boolean queryFlag = true;
	private int orderFactor = 0;

	public Tuple() {
	}

    public Tuple (Tuple copyTuple) {
        for (int i = 0; i < copyTuple.getElementCount(); i++) {
            elements.add(copyTuple.getElement(i));
        }
    }

    public Double[] getElementsAsArray() {
		Double elementArray [] = new Double[elements.size()];
        elements.toArray(elementArray);
        return elementArray;
    }

    public void removeAllElements() {
        elements.clear();
    }

	public void removeElement(int index) {
		elements.remove(index);
	}

	public void moveElement(int currentElementIndex, int newElementIndex) {
		if (currentElementIndex == newElementIndex) {
			return;
		}

		double tmp = elements.get(currentElementIndex);
		if (currentElementIndex < newElementIndex) {
			for (int i = currentElementIndex; i < newElementIndex; i++) {
				elements.set(i, elements.get(i + 1));
			}
		} else {
			for (int i = currentElementIndex; i > newElementIndex; i--) {
				elements.set(i, elements.get(i - 1));
			}
		}
		elements.set(newElementIndex, tmp);
	}

	public void setInstant(Instant instant) {
	    this.instant = Instant.from(instant);
    }

    public Instant getInstant() {
	    return instant;
    }

	public void setElement(int idx, double value) {
		elements.set(idx, value);
	}

	public void addElement(double value) {
		elements.add(value);
	}

	public boolean equals(Tuple tuple) {
		if (tuple.getElementCount() == this.getElementCount()) {
			for (int i = 0; i < this.getElementCount(); i++) {
				if (tuple.getElement(i) != this.getElement(i)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public double getElement(int idx) {
		return elements.get(idx);
	}

	public int getElementCount() {
		return elements.size();
	}

	public void setQueryFlag(boolean enabled) {
		queryFlag = enabled;
	}

	public boolean getQueryFlag() {
		return queryFlag;
	}

	public void setOrderFactor(int order) {
		orderFactor = order;
	}

	public int getOrderFactor() {
		return orderFactor;
	}

	public int compareTo(Object object) {
		int otherOrderFactor = ((Tuple) object).getOrderFactor();
		if (orderFactor < otherOrderFactor) {
			return 1;
		} else if (orderFactor > otherOrderFactor) {
			return -1;
		}
		return 0;
	}
}
