import React, { useState, useEffect } from "react";
import { BACKEND_URL } from './config';

const useItems = () => {
  const [items, itemsSet] = useState([]);

  useEffect(() => {

    async function fetchItems() {
      const fullResponse = await fetch(`${BACKEND_URL}/api`, {
        headers: {"key": "1"}
      });
      const responseJson = await fullResponse.json();
      itemsSet(responseJson);
    }
    fetchItems();
  }, []);

  return [items];
};

export default useItems;