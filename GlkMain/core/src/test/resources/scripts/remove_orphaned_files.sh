#!/bin/bash

#only first part
grep "no db entry for" |

#without .rmtab
sed "/.rmtab$/d" |

#create remove command
sed "s/no db entry for /rm \"/g" |
sed "s/$/\"/g" |

#escape strange file names
sed "s/\`/\\\\\`/g" |

#create deletion for empty directories
sed -r "s:(.*\")(.*/)(.*\"):\1\2\3\nrmdir \"\2\":g" |

#execute
source /dev/stdin