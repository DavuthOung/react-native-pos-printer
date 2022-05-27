type PromiseType = {
    resolve(value: any): Promise<{}>
    reject(reason: any): Promise<{}>
}

export {
    PromiseType
}