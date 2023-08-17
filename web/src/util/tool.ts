export class Tool {

  public static isEmpty (obj: any) {
    if ((typeof obj === 'string')) {
      return !obj || obj.replace(/\s+/g, "") === ""
    } else {
      return (!obj || JSON.stringify(obj) === "{}" || obj.length === 0);
    }
  }


  public static isNotEmpty (obj: any) {
    return !this.isEmpty(obj);
  }


  public static copy (obj: object) {
    if (Tool.isNotEmpty(obj)) {
      return JSON.parse(JSON.stringify(obj));
    }
  }

  /**
   * Convert an array to a tree structure using recursion.
   * The parent ID property is named "parent".
   */
  public static array2Tree (array: any, parentId: number) {
    if (Tool.isEmpty(array)) {
      return [];
    }
    const result = [];
    for (let i = 0; i < array.length; i++) {
      const c = array[i];
      if (Number(c.parent) === Number(parentId)) {
        result.push(c);
        // Recursively view the child nodes
        // corresponding to the current node.
        const children = Tool.array2Tree(array, c.id);
        if (Tool.isNotEmpty(children)) {
          c.children = children;
        }
      }
    }
    return result;
  }


  public static uuid (len: number, radix = 62) {
    const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
    const uuid = [];
    radix = radix || chars.length;

    for (let i = 0; i < len; i++) {
      uuid[i] = chars[0 | Math.random() * radix];
    }

    return uuid.join('');
  }
}


